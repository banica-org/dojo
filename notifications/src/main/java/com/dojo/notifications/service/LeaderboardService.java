package com.dojo.notifications.service;

import com.dojo.notifications.configuration.Configuration;
import com.dojo.notifications.model.client.CustomSlackClient;
import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.model.contest.enums.EventType;
import com.dojo.notifications.model.leaderboard.Leaderboard;
import com.dojo.notifications.model.notification.SlackNotificationUtils;
import com.dojo.notifications.model.user.User;
import com.dojo.notifications.model.user.UserDetails;
import com.hubspot.slack.client.models.blocks.objects.Text;
import com.hubspot.slack.client.models.blocks.objects.TextType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class LeaderboardService {
    private final Configuration configuration;
    private final RestTemplate restTemplate;
    private final UserDetailsService userDetailsService;

    @Autowired
    public LeaderboardService(Configuration configuration, UserDetailsService userDetailsService) {
        this.configuration = configuration;
        this.userDetailsService = userDetailsService;
        this.restTemplate = new RestTemplate();
    }

    public Leaderboard getNewLeaderboardSetup(final Contest contest) {

        UriComponentsBuilder leaderboardApiBuilder = UriComponentsBuilder.fromHttpUrl(configuration.getLeaderboardApi())
                .queryParam("eventId", contest.getContestId())
                .queryParam("userMode", "spectator");

        ResponseEntity<List<User>> responseEntity = restTemplate.exchange(leaderboardApiBuilder.toUriString(),
                HttpMethod.GET, null, new ParameterizedTypeReference<List<User>>() {
                });

        return new Leaderboard(responseEntity.getBody());
    }


    public EventType determineEventType(Leaderboard newLeaderboard, Leaderboard oldLeaderboard) {

        if (IntStream.range(0, Math.min(newLeaderboard.getLeaderboard().size(), oldLeaderboard.getLeaderboard().size()))
                .filter(i -> oldLeaderboard.getLeaderboard().get(i).getUser().getId() != (newLeaderboard.getLeaderboard().get(i).getUser().getId()))
                .findAny().isPresent()) return EventType.POSITION_CHANGES;

        if (IntStream.range(0, Math.min(newLeaderboard.getLeaderboard().size(), oldLeaderboard.getLeaderboard().size()))
                .filter(i -> oldLeaderboard.getLeaderboard().get(i).getScore() != (newLeaderboard.getLeaderboard().get(i).getScore()))
                .findAny().isPresent()) return EventType.SCORE_CHANGES;

        return EventType.OTHER_LEADERBOARD_CHANGE;
    }


    public List<UserDetails> getUserDetails(Leaderboard newLeaderboard, Leaderboard oldLeaderboard) {

        return IntStream.range(0, Math.min(newLeaderboard.getLeaderboard().size(), oldLeaderboard.getLeaderboard().size()))
                .filter(i -> !oldLeaderboard.getLeaderboard().get(i).equals(newLeaderboard.getLeaderboard().get(i)))
                .mapToObj(i -> userDetailsService.getUserDetails(oldLeaderboard.getLeaderboard().get(i).getUser().getId()))
                .collect(Collectors.toList());
    }

    public Text buildLeaderboardNames(Leaderboard leaderboard, UserDetails userDetails, CustomSlackClient slackClient) {
        AtomicInteger position = new AtomicInteger(1);
        StringBuilder names = new StringBuilder();

        leaderboard.getLeaderboard().forEach(user -> {
            String userId = slackClient.getSlackUserId(userDetailsService.getUserEmail(user.getUser().getId()));
            String nameWithLink = "<slack://user?team=null&id=" + userId + "|" + user.getUser().getName() + ">";
            String name = (userDetails != null && user.getUser().getId() == userDetails.getId()) ?
                    SlackNotificationUtils.makeBold(user.getUser().getName()) : userId.isEmpty() ? user.getUser().getName() : nameWithLink;
            names.append(SlackNotificationUtils.makeBold(position.getAndIncrement()))
                    .append(". ")
                    .append(name)
                    .append("\n");
        });
        return Text.of(TextType.MARKDOWN, String.valueOf(names));
    }

    public Text buildLeaderboardScores(Leaderboard leaderboard, UserDetails userDetails) {
        StringBuilder scores = new StringBuilder();

        leaderboard.getLeaderboard().forEach(user -> {
            String score = (userDetails != null && user.getUser().getId() == userDetails.getId()) ? SlackNotificationUtils.makeBold(user.getScore())
                    : String.valueOf(user.getScore());
            scores.append(score).append("\n");
        });
        return Text.of(TextType.MARKDOWN, String.valueOf(scores));
    }

}
