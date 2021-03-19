package com.dojo.notifications.service;

import com.dojo.notifications.configuration.Configuration;
import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.model.contest.enums.EventType;
import com.dojo.notifications.model.leaderboard.Leaderboard;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class LeaderboardService {
    private final Configuration configuration;
    private final RestTemplate restTemplate;
    private final UserDetailsService userDetailsService;

    @Autowired
    public LeaderboardService(Configuration configuration, UserDetailsService userDetailsService, UserDetailsService userDetailsService1) {
        this.configuration = configuration;
        this.userDetailsService = userDetailsService1;
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

}
