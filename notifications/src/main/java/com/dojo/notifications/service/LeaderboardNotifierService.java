package com.dojo.notifications.service;

import com.dojo.notifications.configuration.Configuration;
import com.dojo.notifications.contest.Contest;
import com.dojo.notifications.model.notification.CommonLeaderboardNotification;
import com.dojo.notifications.model.notification.PersonalLeaderboardNotification;
import com.dojo.notifications.model.user.User;
import com.dojo.notifications.model.user.UserDetails;
import com.dojo.notifications.contest.enums.EventType;
import com.dojo.notifications.contest.enums.NotifierType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class LeaderboardNotifierService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LeaderboardNotifierService.class);
    private final Configuration configuration;
    private final RestTemplate restTemplate;
    private final Map<String, List<User>> leaderboards;

    private final Map<NotifierType, NotificationService> notificationServices;
    private final UserDetailsService userDetailsService;

    @Autowired
    public LeaderboardNotifierService(Configuration configuration,
                                      Collection<NotificationService> notificationServices,
                                      UserDetailsService userDetailsService) {
        this.configuration = configuration;
        this.restTemplate = new RestTemplate();
        this.leaderboards = new ConcurrentHashMap<>();
        this.notificationServices = notificationServices.stream()
                .collect(Collectors.toMap(NotificationService::getNotificationServiceTypeMapping, Function.identity()));
        this.userDetailsService = userDetailsService;
    }

    public void getLeaderBoard(final Contest contest) {
        UriComponentsBuilder leaderboardApiBuilder = UriComponentsBuilder.fromHttpUrl(configuration.getLeaderboardApi())
                .queryParam("eventId", contest.getContestId())
                .queryParam("userMode", "spectator");
        ResponseEntity<List<User>> responseEntity = restTemplate.exchange(leaderboardApiBuilder.toUriString(),
                HttpMethod.GET, null, new ParameterizedTypeReference<List<User>>() {
                });

        List<User> newLeaderboard = responseEntity.getBody();
        List<User> oldLeaderboard = leaderboards.get(contest.getContestId());

        if (oldLeaderboard != null && newLeaderboard != null && !newLeaderboard.equals(oldLeaderboard)) {
            notifyAndApplyChanges(contest, newLeaderboard);
        }
        leaderboards.put(contest.getContestId(), newLeaderboard);
    }

    private void notifyAndApplyChanges(final Contest contest, List<User> newLeaderboard) {
        LOGGER.info("There are changes in leaderboard!");

        EventType changesEventType = determineEventType(newLeaderboard, contest);
        if (changesEventType == EventType.POSITION_CHANGES) {
            notifyPersonal(newLeaderboard, contest);
        }
        contest.getCommonNotificationsLevel().entrySet().stream()
                .filter(entry -> entry.getValue().getIncludedEventTypes().contains(changesEventType))
                .forEach(entry -> notifyCommon(newLeaderboard, contest, entry.getKey()));
    }

    private void notifyPersonal(List<User> newLeaderboard, Contest contest) {
        List<User> leaderboard = leaderboards.get(contest.getContestId());

        List<UserDetails> userDetails = IntStream.range(0, Math.min(newLeaderboard.size(), leaderboard.size()))
                .filter(i -> !leaderboard.get(i).equals(newLeaderboard.get(i)))
                .mapToObj(i -> userDetailsService.getUserDetails(leaderboard.get(i).getUser().getId()))
                .collect(Collectors.toList());

        userDetails.forEach(user -> {
            for (NotifierType notifierType : contest.getPersonalNotifiers()) {
                notificationServices.get(notifierType)
                        .notify(user, new PersonalLeaderboardNotification(newLeaderboard, userDetailsService, user), contest);
            }
        });
    }

    private void notifyCommon(List<User> newLeaderboard, Contest contest, NotifierType notifierType) {
        notificationServices.get(notifierType).notify(new CommonLeaderboardNotification(newLeaderboard, userDetailsService), contest);
    }

    private EventType determineEventType(List<User> newLeaderboard, Contest contest) {
        List<User> oldLeaderboard = leaderboards.get(contest.getContestId());

        if (IntStream.range(0, Math.min(newLeaderboard.size(), oldLeaderboard.size()))
                .filter(i -> oldLeaderboard.get(i).getUser().getId() != (newLeaderboard.get(i).getUser().getId()))
                .findAny().isPresent()) return EventType.POSITION_CHANGES;

        if (IntStream.range(0, Math.min(newLeaderboard.size(), oldLeaderboard.size()))
                .filter(i -> oldLeaderboard.get(i).getScore() != (newLeaderboard.get(i).getScore()))
                .findAny().isPresent()) return EventType.SCORE_CHANGES;

        return EventType.OTHER_LEADERBOARD_CHANGE;
    }
}
