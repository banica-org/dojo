package com.dojo.notifications.service;

import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.model.contest.enums.EventType;
import com.dojo.notifications.model.contest.enums.NotifierType;
import com.dojo.notifications.model.leaderboard.Leaderboard;
import com.dojo.notifications.model.notification.CommonLeaderboardNotification;
import com.dojo.notifications.model.notification.PersonalLeaderboardNotification;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(LeaderboardNotifierService.class);

    private final LeaderboardService leaderboardService;
    private final Map<NotifierType, NotificationService> notificationServices;
    private final Map<String, Leaderboard> leaderboards;

    @Autowired
    public LeaderboardNotifierService(LeaderboardService leaderboardService,
                                      Collection<NotificationService> notificationServices) {
        this.leaderboardService = leaderboardService;
        this.leaderboards = new ConcurrentHashMap<>();
        this.notificationServices = notificationServices.stream()
                .collect(Collectors.toMap(NotificationService::getNotificationServiceTypeMapping, Function.identity()));
    }

    public void lookForLeaderboardChanges(final Contest contest) {
        Leaderboard newLeaderboard = leaderboardService.getNewLeaderboardSetup(contest);
        Leaderboard oldLeaderboard = leaderboards.get(contest.getContestId());

        if (oldLeaderboard != null && newLeaderboard != null && !newLeaderboard.equals(oldLeaderboard)) {
            this.notifyAboutChanges(contest, newLeaderboard, oldLeaderboard);
        }

        leaderboards.put(contest.getContestId(), newLeaderboard);
    }

    public void notifyAboutChanges(final Contest contest, Leaderboard newLeaderboard, Leaderboard oldLeaderboard) {
        LOGGER.info(NOTIFYING_MESSAGE);

        EventType changesEventType = leaderboardService.determineEventType(newLeaderboard, oldLeaderboard);

        if (changesEventType == EventType.POSITION_CHANGES) {
            notifyPersonal(contest, newLeaderboard, oldLeaderboard);
        }

        contest.getCommonNotificationsLevel().entrySet().stream()
                .filter(entry -> entry.getValue().getIncludedEventTypes().contains(changesEventType))
                .forEach(entry -> notifyCommon(contest, newLeaderboard, entry.getKey()));
    }

    private void notifyPersonal(Contest contest, Leaderboard newLeaderboard, Leaderboard oldLeaderboard) {

        List<UserDetails> userDetails = leaderboardService.getUserDetails(newLeaderboard, oldLeaderboard);

        userDetails.forEach(user -> {
            for (NotifierType notifierType : contest.getPersonalNotifiers()) {
                notificationServices.get(notifierType)
                        .notify(user, new PersonalLeaderboardNotification(newLeaderboard, user), contest);
            }
        });
    }

    private void notifyCommon(Contest contest, Leaderboard newLeaderboard, NotifierType notifierType) {
        notificationServices.get(notifierType)
                .notify(new CommonLeaderboardNotification(newLeaderboard), contest);
    }

}
