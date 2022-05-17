package com.dojo.codeexecution.service.grpc;

import com.codenjoy.dojo.ScorePayload;
import com.codenjoy.dojo.UpdateScoreServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UpdateScoreService {

    private UpdateScoreServiceGrpc.UpdateScoreServiceBlockingStub updateScoreService;

    public UpdateScoreService(@Value("${grpc.dojoserver.grpc.host}:${grpc.dojoserver.grpc.port}")String dojoServerGrpc){
        ManagedChannel channel = ManagedChannelBuilder.forTarget(dojoServerGrpc).usePlaintext().build();
        updateScoreService = UpdateScoreServiceGrpc.newBlockingStub(channel);
    }


    public void updateScore(String username, String game, int points){
        ScorePayload payload = ScorePayload.newBuilder().setUsername(username).setGame(game).setPoints(points).build();
        updateScoreService.updateUserScore(payload);
    }
}
