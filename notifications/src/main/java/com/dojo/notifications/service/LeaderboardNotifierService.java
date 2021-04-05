package com.dojo.notifications.service;

import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.model.contest.enums.EventType;
import com.dojo.notifications.model.contest.enums.NotifierType;
import com.dojo.notifications.model.leaderboard.Leaderboard;
import com.dojo.notifications.model.notification.CommonLeaderboardNotification;
import com.dojo.notifications.model.notification.PersonalLeaderboardNotification;
import com.dojo.notifications.model.user.Participant;
import com.dojo.notifications.model.user.UserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
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


    public void lookForLeaderboardChanges(final Contest contest, Leaderboard newLeaderboard) {
        Leaderboard oldLeaderboard = leaderboards.get(contest.getContestId());

        List<Participant> newParticipants = newLeaderboard.getSortedParticipants();
        List<Participant> oldParticipants = oldLeaderboard == null ? null : oldLeaderboard.getParticipants();

        if (oldLeaderboard != null
                && !oldParticipants.isEmpty()
                && !newParticipants.isEmpty()
                && !newParticipants.equals(oldParticipants)) {

            try {
                selectRequestService.getRequests().forEach(selectRequest -> {

                            List<Participant> oldQueriedLeaderboard = flinkTableService.executeSingleQuery(oldParticipants, OLD_LEADERBOARD_NAME, selectRequest.getQUERY());

                            List<Participant> newQueriedLeaderboard = flinkTableService.executeSingleQuery(newParticipants, NEW_LEADERBOARD_NAME, selectRequest.getQUERY());

                            if (newQueriedLeaderboard.size() != 0) {
                                notifyAbout(contest, oldQueriedLeaderboard, newQueriedLeaderboard, selectRequest.getEVENT_TYPE(), selectRequest.getMESSAGE());
                            }
                        }
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        leaderboards.put(contest.getContestId(), new Leaderboard(newParticipants));
    }


    public void notifyAbout(Contest contest, List<Participant> oldParticipants, List<Participant> newParticipants, String eventType, String queryMessage) {
        LOGGER.info(NOTIFYING_MESSAGE);

        Leaderboard notifyPeople = new Leaderboard(newParticipants);
        Leaderboard oldLeaderboardN = new Leaderboard(oldParticipants);

        EventType eventTypeQuery = EventType.valueOf(eventType);
        boolean actual = leaderboardService.determineEventType(notifyPeople,oldLeaderboardN, eventTypeQuery);

        if (actual) {
            if(eventTypeQuery.equals(EventType.POSITION_CHANGES)) {
                notifyPersonal(contest, notifyPeople, oldLeaderboardN, queryMessage);
            }

            contest.getCommonNotificationsLevel().entrySet().stream()
                    .filter(entry -> entry.getValue().getIncludedEventTypes().contains(eventTypeQuery))
                    .forEach(entry -> notifyCommon(contest, notifyPeople, entry.getKey(), queryMessage));
        }

    }

    private void notifyPersonal(Contest contest, Leaderboard newLeaderboard, Leaderboard oldLeaderboard, String queryMessage) {

        List<UserDetails> userDetails = leaderboardService.getUserDetails(newLeaderboard, oldLeaderboard);

        userDetails.forEach(user -> {
            for (NotifierType notifierType : contest.getPersonalNotifiers()) {
                notificationServices.get(notifierType)
                        .notify(user, new PersonalLeaderboardNotification(userDetailsService, newLeaderboard, user, queryMessage), contest);
            }
        });
    }

    private void notifyCommon(Contest contest, Leaderboard newLeaderboard, NotifierType notifierType, String queryMessage) {
        notificationServices.get(notifierType)
                .notify(new CommonLeaderboardNotification(userDetailsService, newLeaderboard, queryMessage), contest);
    }

}