package com.dojo.notifications.service;

import com.dojo.notifications.configuration.Configuration;
import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.model.contest.enums.EventType;
import com.dojo.notifications.model.leaderboard.Leaderboard;
import com.dojo.notifications.model.user.Participant;
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
    public LeaderboardService(Configuration configuration, UserDetailsService userDetailsService) {
        this.configuration = configuration;
        this.userDetailsService = userDetailsService;
        this.restTemplate = new RestTemplate();
    }

    public Leaderboard getNewLeaderboardSetup(final Contest contest) {

        //TODO get leaderboard via GRPC and remove Configuration class

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
                .filter(i -> !oldLeaderboard.getUserIdByPosition(i).equals(newLeaderboard.getUserIdByPosition(i)))
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
}
