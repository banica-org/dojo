package com.dojo.notifications.service;

import com.dojo.notifications.configuration.Configuration;
import com.dojo.notifications.contest.Contest;
import com.dojo.notifications.contest.enums.EventType;
import com.dojo.notifications.model.user.User;
import com.dojo.notifications.model.user.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Optional;
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
        this.restTemplate = new RestTemplate();
        this.userDetailsService = userDetailsService;
    }

    public List<User> getNewLeaderboardSetup(final Contest contest) {

        UriComponentsBuilder leaderboardApiBuilder = UriComponentsBuilder.fromHttpUrl(configuration.getLeaderboardApi())
                .queryParam("eventId", contest.getContestId())
                .queryParam("userMode", "spectator");

        ResponseEntity<List<User>> responseEntity = restTemplate.exchange(leaderboardApiBuilder.toUriString(),
                HttpMethod.GET, null, new ParameterizedTypeReference<List<User>>() {
                });

        return responseEntity.getBody();
    }


    public EventType determineEventType(List<User> newLeaderboard, List<User> oldLeaderboard) {

        if (checkForPositionChanges(newLeaderboard, oldLeaderboard)) {
            return EventType.POSITION_CHANGES;
        }

        if (checkForScoreChanges(newLeaderboard, oldLeaderboard)) {
            return EventType.SCORE_CHANGES;
        }

        return EventType.OTHER_LEADERBOARD_CHANGE;
    }


    public List<UserDetails> getUserDetails(List<User> newLeaderboard, List<User> oldLeaderboard) {

        return IntStream.range(0, Math.min(newLeaderboard.size(), oldLeaderboard.size()))
                .filter(i -> !oldLeaderboard.get(i).equals(newLeaderboard.get(i)))
                .mapToObj(i -> userDetailsService.getUserDetails(oldLeaderboard.get(i).getUser().getId()))
                .collect(Collectors.toList());
    }

    private boolean checkForPositionChanges(List<User> newLeaderboard, List<User> oldLeaderboard) {
        return IntStream.range(0, Math.min(newLeaderboard.size(), oldLeaderboard.size()))
                .filter(i -> oldLeaderboard.get(i).getUser().getId() != (newLeaderboard.get(i).getUser().getId()))
                .findAny().isPresent();
    }

    private boolean checkForScoreChanges(List<User> newLeaderboard, List<User> oldLeaderboard) {
        return IntStream.range(0, Math.min(newLeaderboard.size(), oldLeaderboard.size()))
                .filter(i -> oldLeaderboard.get(i).getScore() != (newLeaderboard.get(i).getScore()))
                .findAny().isPresent();
    }

}
