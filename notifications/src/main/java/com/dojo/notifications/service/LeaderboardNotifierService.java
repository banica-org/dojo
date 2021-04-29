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
import org.apache.flink.api.java.tuple.Tuple4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    private static final String RECEIVER_PARTICIPANT = "Participant";
    private static final String RECEIVER_COMMON = "Common";

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

        List<Tuple4<String, String, Integer, Long>> changedUsers = leaderboardService.getLeaderboardChanges(oldLeaderboard, newLeaderboard);

        if (changedUsers.size() != 0) {
            for (SelectRequest request : selectRequestService.getRequests()) {
                Set<String> queriedParticipants = new TreeSet<>();
                try {
                    queriedParticipants = flinkTableService.executeSingleQuery(request, changedUsers);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (queriedParticipants.size() != 0) {
                    notifyAboutCondition(contest, request, queriedParticipants, newLeaderboard);
                }
            }
        }
        leaderboards.put(contest.getContestId(), newLeaderboard);
    }


    private void notifyAboutCondition(Contest contest, SelectRequest request, Set<String> queriedParticipants, Leaderboard newLeaderboard) {
        LOGGER.info(NOTIFYING_MESSAGE);

        EventType eventTypeQuery = EventType.valueOf(request.getEventType());

        if (request.getReceiver().equals(RECEIVER_PARTICIPANT)) {
            notifyContestants(contest, newLeaderboard, queriedParticipants, request.getMessage());

        } else if (request.getReceiver().equals(RECEIVER_COMMON)) {
            contest.getCommonNotificationsLevel().entrySet().stream()
                    .filter(entry -> entry.getValue() != null)
                    .filter(entry -> entry.getValue().getIncludedEventTypes().contains(eventTypeQuery))
                    .forEach(entry -> notifyCommon(contest, newLeaderboard, entry.getKey(), request.getMessage()));

        } else {
            notifyContestants(contest, newLeaderboard, queriedParticipants, request.getMessage());
            contest.getCommonNotificationsLevel().entrySet().stream()
                    .filter(entry -> entry.getValue() != null)
                    .filter(entry -> entry.getValue().getIncludedEventTypes().contains(eventTypeQuery))
                    .forEach(entry -> notifyCommon(contest, newLeaderboard, entry.getKey(), request.getMessage()));

        }
    }


    private void notifyContestants(Contest contest, Leaderboard newLeaderboard, Set<String> userIds, String queryMessage) {
        List<UserDetails> userDetails = new ArrayList<>();
        userIds.forEach(id -> userDetails.add(userDetailsService.getUserDetails(id)));

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

