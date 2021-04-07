package com.dojo.apimock.service;

import com.codenjoy.dojo.UserDetailsRequest;
import com.codenjoy.dojo.UserDetailsResponse;
import com.codenjoy.dojo.UserDetailsServiceGrpc;
import com.dojo.apimock.LeaderBoardProvider;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;

@Service
public class UserDetailsService extends UserDetailsServiceGrpc.UserDetailsServiceImplBase {

    private final LeaderBoardProvider leaderBoardProvider;

    @Autowired
    public UserDetailsService(LeaderBoardProvider leaderBoardProvider) {
        this.leaderBoardProvider = leaderBoardProvider;
    }

    @Override
    public void getUserDetails(UserDetailsRequest request, StreamObserver<UserDetailsResponse> responseObserver) {
        LinkedHashMap<String, String> userDetails = (LinkedHashMap<String, String>) leaderBoardProvider.getUserDetails(request.getId());
        UserDetailsResponse response = UserDetailsResponse.newBuilder()
                .setId(userDetails.get("id"))
                .setEmail(userDetails.get("email"))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
