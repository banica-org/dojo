package com.dojo.notifications.service;

import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.model.contest.enums.EventType;
import com.dojo.notifications.model.contest.enums.NotifierType;
import com.dojo.notifications.model.leaderboard.Leaderboard;
import com.dojo.notifications.model.notification.ParticipantNotification;
import com.dojo.notifications.model.notification.SenseiNotification;
import com.dojo.notifications.model.notification.enums.NotificationType;
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
                notifyParticipant(contest, newQueriedLeaderboard, oldQueriedLeaderboard, queryMessage);
            }

            contest.getCommonNotificationsLevel().entrySet().stream()
                    .filter(entry -> entry.getValue().getIncludedEventTypes().contains(eventTypeQuery))
                    .forEach(entry -> notifySensei(contest, newQueriedLeaderboard, entry.getKey(), queryMessage));
        }

    }

    private void notifyParticipant(Contest contest, Leaderboard newLeaderboard, Leaderboard oldLeaderboard, String queryMessage) {

        List<UserDetails> userDetails = leaderboardService.getUserDetails(newLeaderboard, oldLeaderboard);

        for (UserDetails user : userDetails) {
            for (NotifierType notifierType : contest.getPersonalNotifiers()) {
                notificationServices.get(notifierType)
                        .notify(user, new ParticipantNotification(userDetailsService, newLeaderboard, user, queryMessage, NotificationType.LEADERBOARD), contest);
            }
        }
    }

    private void notifySensei(Contest contest, Leaderboard newLeaderboard, NotifierType notifierType, String queryMessage) {
        notificationServices.get(notifierType)
                .notify(new SenseiNotification(userDetailsService, newLeaderboard, queryMessage, NotificationType.LEADERBOARD), contest);
    }

}
