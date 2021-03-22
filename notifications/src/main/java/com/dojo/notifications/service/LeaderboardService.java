package com.dojo.notifications.service;

import com.dojo.notifications.configuration.Configuration;
import com.dojo.notifications.model.client.CustomSlackClient;
import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.model.contest.enums.EventType;
import com.dojo.notifications.model.leaderboard.Leaderboard;
import com.dojo.notifications.model.notification.SlackNotificationUtils;
import com.dojo.notifications.model.user.Participant;
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

        ResponseEntity<List<Participant>> responseEntity = restTemplate.exchange(leaderboardApiBuilder.toUriString(),
                HttpMethod.GET, null, new ParameterizedTypeReference<List<Participant>>() {
                });

        return new Leaderboard(responseEntity.getBody());
    }


    public EventType determineEventType(Leaderboard newLeaderboard, Leaderboard oldLeaderboard) {

        if (IntStream.range(0, Math.min(newLeaderboard.getParticipantsCount(), oldLeaderboard.getParticipantsCount()))
                .filter(i -> oldLeaderboard.getUserIdByPosition(i) != newLeaderboard.getUserIdByPosition(i))
                .findAny().isPresent()) return EventType.POSITION_CHANGES;

        if (IntStream.range(0, Math.min(newLeaderboard.getParticipantsCount(), oldLeaderboard.getParticipantsCount()))
                .filter(i -> oldLeaderboard.getScoreByPosition(i) != newLeaderboard.getScoreByPosition(i))
                .findAny().isPresent()) return EventType.SCORE_CHANGES;

        return EventType.OTHER_LEADERBOARD_CHANGE;
    }


    public List<UserDetails> getUserDetails(Leaderboard newLeaderboard, Leaderboard oldLeaderboard) {

        return IntStream.range(0, Math.min(newLeaderboard.getParticipantsCount(), oldLeaderboard.getParticipantsCount()))
                .filter(i -> !oldLeaderboard.getParticipantByPosition(i).equals(newLeaderboard.getParticipantByPosition(i)))
                .mapToObj(i -> userDetailsService.getUserDetails(oldLeaderboard.getUserIdByPosition(i)))
                .collect(Collectors.toList());
    }

    public Text buildLeaderboardNames(Leaderboard leaderboard, UserDetails userDetails, CustomSlackClient slackClient) {

        AtomicInteger position = new AtomicInteger(1);
        StringBuilder names = new StringBuilder();

        for (int i = 0; i < leaderboard.getParticipantsCount(); i++) {

            String userId = slackClient.getSlackUserId(userDetailsService.getUserEmail(leaderboard.getUserIdByPosition(i)));
            String nameWithLink = "<slack://user?team=null&id=" + userId + "|" + leaderboard.getNameByPosition(i) + ">";
            String name = (userDetails != null && leaderboard.getUserIdByPosition(i) == userDetails.getId()) ?
                    SlackNotificationUtils.makeBold(leaderboard.getNameByPosition(i)) : userId.isEmpty() ? leaderboard.getNameByPosition(i) : nameWithLink;
            names.append(SlackNotificationUtils.makeBold(position.getAndIncrement()))
                    .append(". ")
                    .append(name)
                    .append("\n");
        }
        return Text.of(TextType.MARKDOWN, String.valueOf(names));
    }

    public Text buildLeaderboardScores(Leaderboard leaderboard, UserDetails userDetails) {

        StringBuilder scores = new StringBuilder();

        for (int i = 0; i < leaderboard.getParticipantsCount(); i++) {

            String score = (userDetails != null && leaderboard.getUserIdByPosition(i) == userDetails.getId()) ? SlackNotificationUtils.makeBold(leaderboard.getScoreByPosition(i))
                    : String.valueOf(leaderboard.getScoreByPosition(i));
            scores.append(score).append("\n");
        }
        return Text.of(TextType.MARKDOWN, String.valueOf(scores));
    }
}
