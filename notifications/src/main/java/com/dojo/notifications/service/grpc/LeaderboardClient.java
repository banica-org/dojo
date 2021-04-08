package com.dojo.notifications.service.grpc;

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

import java.util.ArrayList;
import java.util.List;

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

//TODO not looking good
        final boolean[] isReceived = {false};

        final LeaderboardRequest leaderboardRequest = LeaderboardRequest.newBuilder()
                .setContestId(contestId)
                .build();

        leaderboardServiceStub.getLeaderboard(leaderboardRequest, new StreamObserver<LeaderboardResponse>() {


            @Override
            public void onNext(LeaderboardResponse leaderboardResponse) {
                List<Participant> participants = getParticipants(leaderboardResponse);

                //TODO first time whole leaderboard, after that only updates
                if (!isReceived[0]) {

      //              leaderboardNotifierService.getLeaderboard(contest, new Leaderboard(participants));
                    isReceived[0] = true;

                } else {
                    leaderboardNotifierService.lookForLeaderboardChanges(contest, new Leaderboard(participants));
                }

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

    public void stopLeaderboardNotifications(String contestId) {
        final StopRequest request = StopRequest.newBuilder()
                .setContestId(contestId)
                .build();

        StopResponse response = leaderboardServiceBlockingStub.stopNotifications(request);
        LOGGER.info(NOTIFICATION_STOPPED_MESSAGE, contestId, response);
    }

    private List<Participant> getParticipants(LeaderboardResponse leaderboardResponse) {
        List<Participant> participants = new ArrayList<>();

        leaderboardResponse.getParticipantList().forEach(participantResponse -> {
            UserInfo userInfo = new UserInfo(participantResponse.getId(), participantResponse.getName());
            Participant participant = new Participant(userInfo, participantResponse.getScore());
            participants.add(participant);
        });

        return participants;
    }
}
