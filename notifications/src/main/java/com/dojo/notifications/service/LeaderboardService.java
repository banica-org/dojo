package com.dojo.notifications.service;

import com.dojo.notifications.model.contest.enums.EventType;
import com.dojo.notifications.model.leaderboard.Leaderboard;
import com.dojo.notifications.model.user.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class LeaderboardService {
    private final UserDetailsService userDetailsService;

    @Autowired
    public LeaderboardService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public boolean isEventType(Leaderboard newLeaderboard, Leaderboard oldLeaderboard, EventType eventType) {

        if (IntStream.range(0, Math.min(newLeaderboard.getParticipantsCount(), oldLeaderboard.getParticipantsCount()))
                .filter(i -> !oldLeaderboard.getUserIdByPosition(i).equals(newLeaderboard.getUserIdByPosition(i)))
                .findAny().isPresent() && eventType.equals(EventType.POSITION_CHANGES)) return true;

        if (IntStream.range(0, Math.min(newLeaderboard.getParticipantsCount(), oldLeaderboard.getParticipantsCount()))
                .filter(i -> oldLeaderboard.getScoreByPosition(i) != newLeaderboard.getScoreByPosition(i))
                .findAny().isPresent() && eventType.equals(EventType.SCORE_CHANGES)) return true;

        return eventType.equals(EventType.OTHER_LEADERBOARD_CHANGE);
    }


    public List<UserDetails> getUserDetails(Leaderboard newLeaderboard, Leaderboard oldLeaderboard) {

        return IntStream.range(0, Math.min(newLeaderboard.getParticipantsCount(), oldLeaderboard.getParticipantsCount()))
                .filter(i -> !oldLeaderboard.getUserIdByPosition(i).equals(newLeaderboard.getUserIdByPosition(i))
                        || oldLeaderboard.getScoreByPosition(i)!=newLeaderboard.getScoreByPosition(i))
                .mapToObj(i -> userDetailsService.getUserDetails(oldLeaderboard.getUserIdByPosition(i)))
                .collect(Collectors.toList());
    }
}
