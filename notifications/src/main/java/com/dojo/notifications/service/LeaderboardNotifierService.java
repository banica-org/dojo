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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LeaderboardNotifierService {

    private static final String NOTIFYING_MESSAGE = "There are changes in leaderboard!";
    private static final String NEW_LEADERBOARD_NAME = "NewLeaderboard";
    private static final String OLD_LEADERBOARD_NAME = "OldLeaderboard";

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

    public boolean isBoardRecieved(String contestId) {
        return leaderboards.containsKey(contestId);
    }

    public void getLeaderboardOnStart(String contestId, Leaderboard leaderboard) {
        leaderboards.put(contestId, leaderboard);
    }

    public void getLeaderboardUpdate(final Contest contest, Participant participant) {
        String contestId = contest.getContestId();

        Leaderboard leaderboard = leaderboards.get(contestId);
        leaderboard.updateParticipant(participant);
        lookForLeaderboardChanges(contest, leaderboard);
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

                    Leaderboard oldQueriedLeaderboard = flinkTableService.executeSingleQuery(oldParticipants, OLD_LEADERBOARD_NAME, request.getQuery());

                    Leaderboard newQueriedLeaderboard = flinkTableService.executeSingleQuery(newParticipants, NEW_LEADERBOARD_NAME, request.getQuery());

                    if (newQueriedLeaderboard.getParticipants().size() != 0) {
                        notifyAbout(contest, oldQueriedLeaderboard, newQueriedLeaderboard, request.getEventType(), request.getMessage());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        leaderboards.put(contest.getContestId(), newLeaderboard);
    }


    public void notifyAbout(Contest contest, Leaderboard oldQueriedLeaderboard, Leaderboard newQueriedLeaderboard, String eventType, String queryMessage) {
        LOGGER.info(NOTIFYING_MESSAGE);

        EventType eventTypeQuery = EventType.valueOf(eventType);
        boolean actual = leaderboardService.isEventType(newQueriedLeaderboard, oldQueriedLeaderboard, eventTypeQuery);

        if (actual) {
            if (eventTypeQuery.equals(EventType.POSITION_CHANGES)) {
                notifyPersonal(contest, newQueriedLeaderboard, oldQueriedLeaderboard, queryMessage);
            }

            contest.getCommonNotificationsLevel().entrySet().stream()
                    .filter(entry -> entry.getValue().getIncludedEventTypes().contains(eventTypeQuery))
                    .forEach(entry -> notifyCommon(contest, newQueriedLeaderboard, entry.getKey(), queryMessage));
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
