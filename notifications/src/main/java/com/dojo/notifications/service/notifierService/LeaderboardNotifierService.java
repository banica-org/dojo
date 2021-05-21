package com.dojo.notifications.service.notifierService;

import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.model.contest.enums.NotifierType;
import com.dojo.notifications.model.leaderboard.Leaderboard;
import com.dojo.notifications.model.notification.ParticipantNotification;
import com.dojo.notifications.model.notification.SenseiNotification;
import com.dojo.notifications.model.notification.enums.NotificationType;
import com.dojo.notifications.model.request.SelectRequest;
import com.dojo.notifications.model.user.Participant;
import com.dojo.notifications.model.user.UserDetails;
import com.dojo.notifications.service.FlinkTableService;
import com.dojo.notifications.service.LeaderboardService;
import com.dojo.notifications.service.SelectRequestService;
import com.dojo.notifications.service.UserDetailsService;
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
    private static final String RECEIVER_COMMON = "Common";

    private static final String TABLE_NAME = "leaderboard";

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

        Set<SelectRequest> contestRequests = selectRequestService.getSpecificRequests(contest.getQueryIds(), selectRequestService.getRequestsForTable(TABLE_NAME));

        if (changedUsers.size() != 0) {
            for (SelectRequest request : contestRequests) {
                Set<String> queriedParticipants = new TreeSet<>();
                try {
                    queriedParticipants = flinkTableService.executeLeaderboardQuery(request, changedUsers);
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

        notifyContestants(contest, newLeaderboard, queriedParticipants, request.getMessage());
        if (request.getReceivers() != null) {
            notifyListeners(contest, newLeaderboard, userDetailsService.turnUsersToUserIds(request.getReceivers()), queriedParticipants, request.getMessage());
            if (request.getReceivers().contains(RECEIVER_COMMON)) {
                contest.getNotifiers()
                        .forEach(notifierType -> notifySensei(contest, newLeaderboard, notifierType, request.getMessage()));
            }
        }
    }

    private void notifyListeners(Contest contest, Leaderboard newLeaderboard, Set<String> eventListenerIds, Set<String> queried, String queryMessage) {
        List<UserDetails> userDetails = new ArrayList<>();
        eventListenerIds.forEach(id -> userDetails.add(userDetailsService.getUserDetailsById(id)));

        List<UserDetails> queriedUserDetails = new ArrayList<>();
        queried.forEach(id -> queriedUserDetails.add(userDetailsService.getUserDetailsById(id)));

        for (UserDetails user : userDetails) {
            for (NotifierType notifierType : contest.getNotifiers()) {
                queriedUserDetails.forEach(changedUserDetail -> notificationServices.get(notifierType)
                        .notify(user, new ParticipantNotification(userDetailsService, newLeaderboard, changedUserDetail, queryMessage, NotificationType.LEADERBOARD), contest));
            }
        }
    }

    private void notifyContestants(Contest contest, Leaderboard newLeaderboard, Set<String> userIds, String queryMessage) {
        List<UserDetails> userDetails = new ArrayList<>();
        userIds.forEach(id -> userDetails.add(userDetailsService.getUserDetailsById(id)));

        for (UserDetails user : userDetails) {
            for (NotifierType notifierType : contest.getNotifiers()) {
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
