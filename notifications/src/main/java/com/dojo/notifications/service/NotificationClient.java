package com.dojo.notifications.service;

import com.dojo.apimock.ApiMockServiceGrpc;
import com.dojo.apimock.LeaderboardRequest;
import com.dojo.apimock.LeaderboardResponse;
import com.dojo.apimock.StartRequest;
import com.dojo.apimock.StartResponse;
import com.dojo.apimock.StopRequest;
import com.dojo.apimock.StopResponse;
import com.dojo.notifications.model.contest.Contest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class NotificationClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationClient.class);

    private final ScheduledExecutorService scheduledExecutorService;
    private final ApiMockServiceGrpc.ApiMockServiceBlockingStub blockingStub;

    @Autowired
    public NotificationClient(ApiMockServiceGrpc.ApiMockServiceBlockingStub blockingStub) {
        this.blockingStub = blockingStub;
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    }

    public void startLeaderboardNotifications(Contest contest) {
        final LeaderboardRequest leaderboardRequest = LeaderboardRequest.newBuilder()
                .setContestId(contest.getContestId())
                .build();

        final StartRequest startRequest = StartRequest.newBuilder()
                .setContestId(contest.getContestId())
                .build();

        StartResponse response =blockingStub.startNotifications(startRequest);

        scheduledExecutorService.scheduleAtFixedRate(() -> {
            Iterator<LeaderboardResponse> iterator = blockingStub.getLeaderboard(leaderboardRequest);
            while (iterator.hasNext()) {
                LOGGER.info("Response: {}", iterator.next());
            }
        }, 2, 2, TimeUnit.SECONDS);

    }

    public void stopLeaderboardNotifications(String contestId) {
        final StopRequest request = StopRequest.newBuilder()
                .setContestId(contestId)
                .build();
        StopResponse response = blockingStub.stopNotifications(request);
    }
}
