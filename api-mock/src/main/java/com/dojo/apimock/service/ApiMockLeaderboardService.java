package com.dojo.apimock.service;


import com.dojo.apimock.ApiMockLeaderboardServiceGrpc;
import com.dojo.apimock.LeaderBoardProvider;
import com.dojo.apimock.LeaderboardRequest;
import com.dojo.apimock.LeaderboardResponse;
import com.dojo.apimock.Participant;
import com.dojo.apimock.StartRequest;
import com.dojo.apimock.StartResponse;
import com.dojo.apimock.StopRequest;
import com.dojo.apimock.StopResponse;
import com.dojo.apimock.UserInfo;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class ApiMockLeaderboardService extends ApiMockLeaderboardServiceGrpc.ApiMockLeaderboardServiceImplBase {

    private final LeaderBoardProvider leaderBoardProvider;

    private final List<String> subscriptions;

    @Autowired
    public ApiMockLeaderboardService(LeaderBoardProvider leaderBoardProvider) {
        this.leaderBoardProvider = leaderBoardProvider;
        this.subscriptions = new ArrayList<>();
    }

    @Override
    public void getLeaderboard(LeaderboardRequest request, StreamObserver<LeaderboardResponse> responseObserver) {

        AtomicInteger requestNumber = new AtomicInteger(1);
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

    private LeaderboardResponse getNextLeaderboard(int requestNumber, String contestId) {
        List<LinkedHashMap<String, Object>> leaderboard = generateLeaderboard(requestNumber, contestId);

        return LeaderboardResponse.newBuilder()
                .addAllParticipant(leaderboard.stream().map(obj -> {
                    String id = (String) obj.get("id");
                    String name = (String) obj.get("name");
                    long score = new Long((Integer) obj.get("score"));
                    return Participant.newBuilder()
                            .setUser(UserInfo.newBuilder()
                                    .setId(id)
                                    .setName(name).build())
                            .setScore(score)
                            .build();
                }).collect(Collectors.toList()))
                .build();
    }

    @Override
    public void stopNotifications(StopRequest request, StreamObserver<StopResponse> responseObserver) {
        String contestId = request.getContestId();
        subscriptions.remove(contestId);
        responseObserver.onNext(StopResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void startNotifications(StartRequest request, StreamObserver<StartResponse> responseObserver) {
        String contestId = request.getContestId();
        if (!subscriptions.contains(contestId)) {
            subscriptions.add(contestId);
        }

        responseObserver.onNext(StartResponse.newBuilder().build());
        responseObserver.onCompleted();
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
