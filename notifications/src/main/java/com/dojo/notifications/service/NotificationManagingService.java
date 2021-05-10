package com.dojo.notifications.service;

import com.dojo.notifications.grpc.DockerClient;
import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.grpc.LeaderboardClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationManagingService {

    private final LeaderboardClient leaderboardClient;
    private final DockerClient dockerClient;


    @Autowired
    public NotificationManagingService(LeaderboardClient leaderboardClient, DockerClient dockerClient) {
        this.leaderboardClient = leaderboardClient;
        this.dockerClient = dockerClient;
    }

    public void startNotifications(final Contest contest) {
        leaderboardClient.startLeaderboardNotifications(contest);
        dockerClient.startDockerNotifications(contest);
    }

    public void stopNotifications(Contest contest) {
        leaderboardClient.stopLeaderboardNotifications(contest);
        dockerClient.stopDockerNotifications(contest);
    }
}
