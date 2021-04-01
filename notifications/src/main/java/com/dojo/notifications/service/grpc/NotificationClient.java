package com.dojo.notifications.service.grpc;

import com.dojo.apimock.ApiMockServiceGrpc;
import com.dojo.apimock.LeaderboardRequest;
import com.dojo.apimock.LeaderboardResponse;
import com.dojo.apimock.StartRequest;
import com.dojo.apimock.StartResponse;
import com.dojo.apimock.StopRequest;
import com.dojo.apimock.StopResponse;
import com.dojo.notifications.model.contest.Contest;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NotificationClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationClient.class);

    private final ApiMockServiceGrpc.ApiMockServiceBlockingStub blockingStub;
    private final ApiMockServiceGrpc.ApiMockServiceStub stub;

    @Autowired
    public NotificationClient(ApiMockServiceGrpc.ApiMockServiceBlockingStub blockingStub, ApiMockServiceGrpc.ApiMockServiceStub stub) {
        this.blockingStub = blockingStub;
        this.stub = stub;

    }

    public void startLeaderboardNotifications(Contest contest) {
        String contestId = contest.getContestId();

        subscribeForNotifications(contestId);
        getNotifications(contestId);
    }

    public void stopLeaderboardNotifications(String contestId) {
        final StopRequest request = StopRequest.newBuilder()
                .setContestId(contestId)
                .build();

        StopResponse response = blockingStub.stopNotifications(request);
        LOGGER.info("Notifications stopped for contest {} {}", contestId, response);
    }

    private void getNotifications(String contestId) {
        final LeaderboardRequest leaderboardRequest = LeaderboardRequest.newBuilder()
                .setContestId(contestId)
                .build();

        stub.getLeaderboard(leaderboardRequest, new StreamObserver<LeaderboardResponse>() {
            @Override
            public void onNext(LeaderboardResponse leaderboardResponse) {
                LOGGER.info("Response: {}", leaderboardResponse);
            }

            @Override
            public void onError(Throwable throwable) {
                LOGGER.error("Unable to request {}", leaderboardRequest, throwable);
            }

            @Override
            public void onCompleted() {
                LOGGER.info("Completed.");
            }
        });
    }

    private void subscribeForNotifications(String contestId) {
        final StartRequest startRequest = StartRequest.newBuilder()
                .setContestId(contestId)
                .build();

        StartResponse response = blockingStub.startNotifications(startRequest);
        LOGGER.info("Notifications started for contest {} {}", contestId, response);
    }
}
