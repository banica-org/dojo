package com.dojo.apimock.service;

import com.dojo.apimock.ApiMockUserDetailsServiceGrpc;
import com.dojo.apimock.LeaderBoardProvider;
import com.dojo.apimock.UserDetailsRequest;
import com.dojo.apimock.UserDetailsResponse;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;

@Service
public class ApiMockUserDetailsService extends ApiMockUserDetailsServiceGrpc.ApiMockUserDetailsServiceImplBase {

    private final LeaderBoardProvider leaderBoardProvider;

    @Autowired
    public ApiMockUserDetailsService(LeaderBoardProvider leaderBoardProvider) {
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
