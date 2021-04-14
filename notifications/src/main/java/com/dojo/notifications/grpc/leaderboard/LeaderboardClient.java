package com.dojo.notifications.grpc.leaderboard;

import com.codenjoy.dojo.LeaderboardRequest;
import com.codenjoy.dojo.LeaderboardResponse;
import com.codenjoy.dojo.LeaderboardServiceGrpc;
import com.codenjoy.dojo.StopRequest;
import com.codenjoy.dojo.StopResponse;
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

import java.util.Set;
import java.util.TreeSet;


@Component
public class LeaderboardClient {

    private static final String RESPONSE_MESSAGE = "Response: {}";
    private static final String NOTIFICATIONS_STARTED_MESSAGE = "Notifications started for contest {}";
    private static final String NOTIFICATION_STOPPED_MESSAGE = "Notifications stopped for contest {} {}";
    private static final String ERROR_MESSAGE = "Unable to request {}";
    private static final String COMPLETED_MESSAGE = "Completed.";

    private static final Logger LOGGER = LoggerFactory.getLogger(LeaderboardClient.class);

    private final LeaderboardNotifierService leaderboardNotifierService;

    private final LeaderboardServiceGrpc.LeaderboardServiceBlockingStub leaderboardServiceBlockingStub;
    private final LeaderboardServiceGrpc.LeaderboardServiceStub leaderboardServiceStub;

    @Autowired
    public LeaderboardClient(LeaderboardNotifierService leaderboardNotifierService, LeaderboardServiceGrpc.LeaderboardServiceBlockingStub leaderboardServiceBlockingStub, LeaderboardServiceGrpc.LeaderboardServiceStub leaderboardServiceStub) {
        this.leaderboardNotifierService = leaderboardNotifierService;
        this.leaderboardServiceBlockingStub = leaderboardServiceBlockingStub;
        this.leaderboardServiceStub = leaderboardServiceStub;
    }

    public void startLeaderboardNotifications(Contest contest) {
        String contestId = contest.getContestId();
        LOGGER.info(NOTIFICATIONS_STARTED_MESSAGE, contestId);

        sendLeaderboardUpdates(contest);
    }

    public void stopLeaderboardNotifications(String contestId) {
        final StopRequest request = StopRequest.newBuilder()
                .setContestId(contestId)
                .build();

        StopResponse response = leaderboardServiceBlockingStub.stopNotifications(request);
        LOGGER.info(NOTIFICATION_STOPPED_MESSAGE, contestId, response);
    }

    private void sendLeaderboardUpdates(Contest contest) {
        final LeaderboardRequest leaderboardRequest = LeaderboardRequest.newBuilder()
                .setContestId(contest.getContestId())
                .build();

        leaderboardServiceStub.getLeaderboard(leaderboardRequest, new StreamObserver<LeaderboardResponse>() {
            @Override
            public void onNext(LeaderboardResponse leaderboardResponse) {
                LOGGER.info(RESPONSE_MESSAGE, leaderboardResponse);

                String contestId = contest.getContestId();
                if (!leaderboardNotifierService.isLeaderboardReceived(contestId)) {
                    leaderboardNotifierService.setLeaderboardOnStart(contestId, getLeaderboard(leaderboardResponse));
                } else {
                    leaderboardResponse.getParticipantList().forEach(participantResponse -> leaderboardNotifierService.updateLeaderboard(contest, convertToParticipant(participantResponse)));
                }
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

    private Leaderboard getLeaderboard(LeaderboardResponse leaderboardResponse) {
        Set<Participant> participants = new TreeSet<>();

        leaderboardResponse.getParticipantList().forEach(participantResponse -> participants.add(convertToParticipant(participantResponse)));
        return new Leaderboard(participants);
    }

    private Participant convertToParticipant(com.codenjoy.dojo.Participant participantResponse) {
        UserInfo userInfo = new UserInfo(participantResponse.getId(), participantResponse.getName());
        return new Participant(userInfo, participantResponse.getScore());
    }
}
