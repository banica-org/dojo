package com.dojo.notifications.service.grpc;

import com.dojo.apimock.ApiMockLeaderboardServiceGrpc;
import com.dojo.apimock.LeaderboardRequest;
import com.dojo.apimock.LeaderboardResponse;
import com.dojo.apimock.StartRequest;
import com.dojo.apimock.StartResponse;
import com.dojo.apimock.StopRequest;
import com.dojo.apimock.StopResponse;
import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.model.leaderboard.Leaderboard;
import com.dojo.notifications.model.user.Participant;
import com.dojo.notifications.model.user.UserInfo;
import com.dojo.notifications.service.LeaderboardNotifierService;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class LeaderboardClient {

    private static final String RESPONSE_MESSAGE = "Response: {}";
    private static final String NOTIFICATIONS_STARTED_MESSAGE = "Notifications started for contest {} {}";
    private static final String NOTIFICATION_STOPPED_MESSAGE = "Notifications stopped for contest {} {}";
    private static final String ERROR_MESSAGE = "Unable to request {}";
    private static final String COMPLETED_MESSAGE = "Completed.";

    private static final Logger LOGGER = LoggerFactory.getLogger(LeaderboardClient.class);

    private final LeaderboardNotifierService leaderboardNotifierService;

    private final ApiMockLeaderboardServiceGrpc.ApiMockLeaderboardServiceBlockingStub leaderboardServiceBlockingStub;
    private final ApiMockLeaderboardServiceGrpc.ApiMockLeaderboardServiceStub leaderboardServiceStub;

    @Autowired
    public LeaderboardClient(LeaderboardNotifierService leaderboardNotifierService, ApiMockLeaderboardServiceGrpc.ApiMockLeaderboardServiceBlockingStub leaderboardServiceBlockingStub, ApiMockLeaderboardServiceGrpc.ApiMockLeaderboardServiceStub leaderboardServiceStub) {
        this.leaderboardNotifierService = leaderboardNotifierService;

        this.leaderboardServiceBlockingStub = leaderboardServiceBlockingStub;
        this.leaderboardServiceStub = leaderboardServiceStub;
    }

    public void startLeaderboardNotifications(Contest contest) {
        String contestId = contest.getContestId();
        subscribeForNotifications(contestId);
        getNotifications(contest);
    }

    public void stopLeaderboardNotifications(String contestId) {
        final StopRequest request = StopRequest.newBuilder()
                .setContestId(contestId)
                .build();

        StopResponse response = leaderboardServiceBlockingStub.stopNotifications(request);
        LOGGER.info(NOTIFICATION_STOPPED_MESSAGE, contestId, response);
    }

    private void subscribeForNotifications(String contestId) {
        final StartRequest startRequest = StartRequest.newBuilder()
                .setContestId(contestId)
                .build();

        StartResponse response = leaderboardServiceBlockingStub.startNotifications(startRequest);
        LOGGER.info(NOTIFICATIONS_STARTED_MESSAGE, contestId, response);
    }

    private void getNotifications(Contest contest) {
        final LeaderboardRequest leaderboardRequest = LeaderboardRequest.newBuilder()
                .setContestId(contest.getContestId())
                .build();

        leaderboardServiceStub.getLeaderboard(leaderboardRequest, new StreamObserver<LeaderboardResponse>() {
            @Override
            public void onNext(LeaderboardResponse leaderboardResponse) {
                Set<Participant> participants = getParticipants(leaderboardResponse);

                leaderboardNotifierService.lookForLeaderboardChanges(contest, new Leaderboard(participants));
                LOGGER.info(RESPONSE_MESSAGE, leaderboardResponse);
            }

            @Override
            public void onError(Throwable throwable) {
                LOGGER.error(ERROR_MESSAGE, leaderboardRequest, throwable);
            }

            @Override
            public void onCompleted() {
                LOGGER.info(COMPLETED_MESSAGE);
            }
        });
    }

    private TreeSet<Participant> getParticipants(LeaderboardResponse leaderboardResponse) {
        TreeSet<Participant> participants = new TreeSet<>();


        leaderboardResponse.getParticipantList().forEach(participantResponse -> {
            UserInfo userInfo = new UserInfo(participantResponse.getId(), participantResponse.getName());
            Participant participant = new Participant(userInfo, participantResponse.getScore());
            participants.add(participant);
        });

        return participants;
    }
}
