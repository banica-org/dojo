package com.dojo.notifications.service;

import com.dojo.notifications.model.leaderboard.Leaderboard;
import com.dojo.notifications.model.user.Participant;
import org.apache.flink.api.java.tuple.Tuple4;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LeaderboardService {

    public List<Tuple4<String, String, Integer, Long>> getLeaderboardChanges(Leaderboard oldLeaderboard, Leaderboard newLeaderboard) {
        List<Tuple4<String, String, Integer, Long>> changedUsers = new ArrayList<>();
        for (Participant p : newLeaderboard.getParticipants()) {
            String id = p.getUser().getId();
            int oldPos = oldLeaderboard.getPositionByUserId(id);
            int newPos = newLeaderboard.getPositionByUserId(id);

            long oldScore = oldLeaderboard.getScoreByUserId(id);
            long newScore = p.getScore();

            if (newPos != oldPos || newScore != oldScore) {
                changedUsers.add(new Tuple4<>(id, p.getUser().getName(), oldPos - newPos, newScore - oldScore));
            }
        }
        return changedUsers;
    }
}
