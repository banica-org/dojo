package com.dojo.notifications.service;

import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.grpc.leaderboard.LeaderboardClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationManagingService {

    private final LeaderboardClient leaderboardClient;


    @Autowired
    public NotificationManagingService(LeaderboardClient leaderboardClient) {
        this.leaderboardClient = leaderboardClient;
    }

    public void startNotifications(final Contest contest) {
        leaderboardClient.startLeaderboardNotifications(contest);
    }

    public void stopNotifications(final String contestId) {
        leaderboardClient.stopLeaderboardNotifications(contestId);
    }
}
