package com.dojo.notifications.service.grpc;

import com.dojo.apimock.ApiMockLeaderboardServiceGrpc;
import com.dojo.apimock.ApiMockUserDetailsServiceGrpc;
import com.dojo.apimock.LeaderboardRequest;
import com.dojo.apimock.LeaderboardResponse;
import com.dojo.apimock.StartRequest;
import com.dojo.apimock.StopRequest;
import com.dojo.apimock.UserDetailsRequest;
import com.dojo.apimock.UserDetailsResponse;
import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.model.user.UserDetails;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NotificationClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationClient.class);

    private final ApiMockLeaderboardServiceGrpc.ApiMockLeaderboardServiceBlockingStub leaderboardServiceBlockingStub;
    private final ApiMockLeaderboardServiceGrpc.ApiMockLeaderboardServiceStub leaderboardServiceStub;

    private final ApiMockUserDetailsServiceGrpc.ApiMockUserDetailsServiceBlockingStub userDetailsServiceBlockingStub;

    @Autowired
    public NotificationClient(ApiMockLeaderboardServiceGrpc.ApiMockLeaderboardServiceBlockingStub leaderboardServiceBlockingStub, ApiMockLeaderboardServiceGrpc.ApiMockLeaderboardServiceStub leaderboardServiceStub, ApiMockUserDetailsServiceGrpc.ApiMockUserDetailsServiceBlockingStub userDetailsServiceBlockingStub) {
        this.leaderboardServiceBlockingStub = leaderboardServiceBlockingStub;
        this.leaderboardServiceStub = leaderboardServiceStub;

        this.userDetailsServiceBlockingStub = userDetailsServiceBlockingStub;
    }

    public UserDetails getUserDetails(String userId) {
        UserDetailsRequest request = UserDetailsRequest.newBuilder().setId(userId).build();
        UserDetailsResponse response = userDetailsServiceBlockingStub.getUserDetails(request);

        UserDetails userDetails = new UserDetails();
        userDetails.setId(response.getId());
        userDetails.setEmail(response.getEmail());

        return userDetails;
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
        LOGGER.info("Notifications stopped for contest {} {}", contestId, leaderboardServiceBlockingStub.stopNotifications(request));
    }

    private void getNotifications(String contestId) {
        final LeaderboardRequest leaderboardRequest = LeaderboardRequest.newBuilder()
                .setContestId(contestId)
                .build();

        leaderboardServiceStub.getLeaderboard(leaderboardRequest, new StreamObserver<LeaderboardResponse>() {
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

        LOGGER.info("Notifications started for contest {} {}", contestId, leaderboardServiceBlockingStub.startNotifications(startRequest));
    }
}
