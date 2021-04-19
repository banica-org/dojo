package com.dojo.notifications.service;

import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.model.contest.enums.EventType;
import com.dojo.notifications.model.contest.enums.NotifierType;
import com.dojo.notifications.model.leaderboard.Leaderboard;
import com.dojo.notifications.model.notification.CommonLeaderboardNotification;
import com.dojo.notifications.model.notification.PersonalLeaderboardNotification;
import com.dojo.notifications.model.request.SelectRequest;
import com.dojo.notifications.model.user.Participant;
import com.dojo.notifications.model.user.UserDetails;
import com.dojo.notifications.service.notificationService.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LeaderboardNotifierService {

    private static final String NOTIFYING_MESSAGE = "There are changes in leaderboard!";

    private static final Logger LOGGER = LoggerFactory.getLogger(LeaderboardNotifierService.class);

    private final UserDetailsService userDetailsService;
    private final LeaderboardService leaderboardService;
    private final SelectRequestService selectRequestService;
    private final FlinkTableService flinkTableService;
    private final Map<NotifierType, NotificationService> notificationServices;
    private final Map<String, Leaderboard> leaderboards;

    @Autowired
    public LeaderboardNotifierService(UserDetailsService userDetailsService, LeaderboardService leaderboardService,
                                      SelectRequestService selectRequestService, FlinkTableService flinkTableService, Collection<NotificationService> notificationServices) {
        this.userDetailsService = userDetailsService;
        this.leaderboardService = leaderboardService;
        this.selectRequestService = selectRequestService;
        this.flinkTableService = flinkTableService;
        this.leaderboards = new ConcurrentHashMap<>();
        this.notificationServices = notificationServices.stream()
                .collect(Collectors.toMap(NotificationService::getNotificationServiceTypeMapping, Function.identity()));
    }

    public boolean isLeaderboardReceived(String contestId) {
        return leaderboards.containsKey(contestId);
    }

    public void setLeaderboardOnStart(String contestId, Leaderboard leaderboard) {
        leaderboards.put(contestId, leaderboard);
    }

    public void updateLeaderboard(final Contest contest, Participant participant) {
        String contestId = contest.getContestId();

        Leaderboard leaderboard = leaderboards.get(contestId);
        Set<Participant> participants = new TreeSet<>(leaderboard.getParticipants());
        Leaderboard newLeaderboard = new Leaderboard(participants);
        newLeaderboard.updateParticipant(participant);
        lookForLeaderboardChanges(contest, newLeaderboard);
    }


    public void lookForLeaderboardChanges(final Contest contest, Leaderboard newLeaderboard) {
        Leaderboard oldLeaderboard = leaderboards.get(contest.getContestId());

        if (oldLeaderboard != null
                && newLeaderboard != null
                && !oldLeaderboard.getParticipants().isEmpty()
                && !oldLeaderboard.equals(newLeaderboard)) {

            Set<Participant> newParticipants = newLeaderboard.getParticipants();
            Set<Participant> oldParticipants = oldLeaderboard.getParticipants();

            try {
                for (SelectRequest request : selectRequestService.getRequests()) {

                    Leaderboard oldQueriedLeaderboard = flinkTableService.executeSingleQuery(oldParticipants, request.getQuery());

                    Leaderboard newQueriedLeaderboard = flinkTableService.executeSingleQuery(newParticipants, request.getQuery());

                    if (newQueriedLeaderboard.getParticipants().size() != 0
                            && !oldQueriedLeaderboard.equals(newQueriedLeaderboard)) {
                        notifyAbout(contest, oldQueriedLeaderboard, newQueriedLeaderboard, request);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        leaderboards.put(contest.getContestId(), newLeaderboard);
    }


    public void notifyAbout(Contest contest, Leaderboard oldQueriedLeaderboard, Leaderboard newQueriedLeaderboard, SelectRequest request) {
        LOGGER.info(NOTIFYING_MESSAGE);

        EventType eventTypeQuery = EventType.valueOf(request.getEventType());
        boolean actual = leaderboardService.isEventType(newQueriedLeaderboard, oldQueriedLeaderboard, eventTypeQuery);

        if (actual) {
            if (request.getReceiver().equals("All")) {
                notifyPersonal(contest, newQueriedLeaderboard, oldQueriedLeaderboard, request.getMessage());
                contest.getCommonNotificationsLevel().entrySet().stream()
                        .filter(entry -> entry.getValue().getIncludedEventTypes().contains(eventTypeQuery))
                        .forEach(entry -> notifyCommon(contest, newQueriedLeaderboard, entry.getKey(), request.getMessage()));
            } else if (request.getReceiver().equals("Participant")) {
                notifyPersonal(contest, newQueriedLeaderboard, oldQueriedLeaderboard, request.getMessage());
            } else {
                contest.getCommonNotificationsLevel().entrySet().stream()
                        .filter(entry -> entry.getValue().getIncludedEventTypes().contains(eventTypeQuery))
                        .forEach(entry -> notifyCommon(contest, newQueriedLeaderboard, entry.getKey(), request.getMessage()));
            }
        }

    }

    private void notifyPersonal(Contest contest, Leaderboard newLeaderboard, Leaderboard oldLeaderboard, String queryMessage) {

        List<UserDetails> userDetails = leaderboardService.getUserDetails(newLeaderboard, oldLeaderboard);

        for (UserDetails user : userDetails) {
            for (NotifierType notifierType : contest.getPersonalNotifiers()) {
                notificationServices.get(notifierType)
                        .notify(user, new PersonalLeaderboardNotification(userDetailsService, newLeaderboard, user, queryMessage), contest);
            }
        }
    }

    private void notifyCommon(Contest contest, Leaderboard newLeaderboard, NotifierType notifierType, String queryMessage) {
        notificationServices.get(notifierType)
                .notify(new CommonLeaderboardNotification(userDetailsService, newLeaderboard, queryMessage), contest);
    }

}
