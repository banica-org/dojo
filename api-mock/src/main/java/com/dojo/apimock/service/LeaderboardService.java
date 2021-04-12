package com.dojo.apimock.service;


import com.codenjoy.dojo.LeaderboardRequest;
import com.codenjoy.dojo.LeaderboardResponse;
import com.codenjoy.dojo.LeaderboardServiceGrpc;
import com.codenjoy.dojo.Participant;
import com.codenjoy.dojo.StopRequest;
import com.codenjoy.dojo.StopResponse;
import com.dojo.apimock.LeaderBoardProvider;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class LeaderboardService extends LeaderboardServiceGrpc.LeaderboardServiceImplBase {

    private final LeaderBoardProvider leaderBoardProvider;

    private final List<String> subscriptions;

    @Autowired
    public LeaderboardService(LeaderBoardProvider leaderBoardProvider) {
        this.leaderBoardProvider = leaderBoardProvider;
        this.subscriptions = new ArrayList<>();
    }

    @Override
    public void getLeaderboard(LeaderboardRequest request, StreamObserver<LeaderboardResponse> responseObserver) {

        AtomicInteger requestNumber = new AtomicInteger(1);
        this.subscriptions.add(request.getContestId());
        responseObserver.onNext(getNextLeaderboard(0, request.getContestId()));

        while (subscriptions.contains(request.getContestId())) {
            responseObserver.onNext(getNextLeaderboard(requestNumber.getAndIncrement(), request.getContestId()));
            try {
                Thread.sleep(5000);
            } catch (InterruptedException exception) {
                exception.printStackTrace();
            }
        }

        responseObserver.onCompleted();
    }

    @Override
    public void stopNotifications(StopRequest request, StreamObserver<StopResponse> responseObserver) {
        String contestId = request.getContestId();
        subscriptions.remove(contestId);
        responseObserver.onNext(StopResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    private LeaderboardResponse getNextLeaderboard(int requestNumber, String contestId) {
        List<LinkedHashMap<String, Object>> leaderboard = generateLeaderboard(requestNumber, contestId);

        return LeaderboardResponse.newBuilder()
                .addAllParticipant(leaderboard.stream().map(obj -> {
                    String id = (String) obj.get("id");
                    String name = (String) obj.get("name");
                    long score = new Long((Integer) obj.get("score"));
                    return Participant.newBuilder()
                            .setId(id)
                            .setName(name)
                            .setScore(score)
                            .build();
                }).collect(Collectors.toList()))
                .build();
    }

    private List<LinkedHashMap<String, Object>> generateLeaderboard(int requestNumber, String contestId) {
        List<Object> objects = leaderBoardProvider.generateLeaderBoard(requestNumber, contestId);

        List<LinkedHashMap<String, Object>> leaderboard = new ArrayList<>();
        objects.forEach(obj -> {
            LinkedHashMap<String, Object> participant = (LinkedHashMap<String, Object>) obj;
            leaderboard.add(participant);
        });
        return leaderboard;
    }
}
