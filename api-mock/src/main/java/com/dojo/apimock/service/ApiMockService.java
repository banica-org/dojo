package com.dojo.apimock.service;


import com.dojo.apimock.ApiMockServiceGrpc;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class ApiMockService extends ApiMockServiceGrpc.ApiMockServiceImplBase {

    private static final int INITIAL_DELAY = 0;
    private static final int SCHEDULE_PERIOD = 5;
    private static final int POOL_SIZE = 1;

    private final LeaderBoardProvider leaderBoardProvider;
    private final ScheduledExecutorService executorService;

    private final List<String> subscriptions;

    @Autowired
    public ApiMockService(LeaderBoardProvider leaderBoardProvider) {
        this.leaderBoardProvider = leaderBoardProvider;
        this.executorService = Executors.newScheduledThreadPool(POOL_SIZE);
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
        List<LinkedHashMap<String, Object>> objects = leaderBoardProvider.generateLeaderBoard(requestNumber, contestId);
        return LeaderboardResponse.newBuilder()
                .addAllParticipant(objects.stream().map(obj ->{
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
}
