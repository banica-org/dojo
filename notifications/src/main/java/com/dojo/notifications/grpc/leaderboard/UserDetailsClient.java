package com.dojo.notifications.grpc.leaderboard;

import com.codenjoy.dojo.UserDetailsRequest;
import com.codenjoy.dojo.UserDetailsResponse;
import com.codenjoy.dojo.UserDetailsServiceGrpc;
import com.dojo.notifications.model.user.UserDetails;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserDetailsClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDetailsClient.class);

    private final UserDetailsServiceGrpc.UserDetailsServiceBlockingStub userDetailsServiceBlockingStub;

    @Autowired
    public UserDetailsClient(UserDetailsServiceGrpc.UserDetailsServiceBlockingStub userDetailsServiceBlockingStub) {
        this.userDetailsServiceBlockingStub = userDetailsServiceBlockingStub;
    }


    public UserDetails getUserDetails(String userId) {
        UserDetailsRequest request = UserDetailsRequest.newBuilder().setId(userId).build();
        try {
            UserDetailsResponse response = userDetailsServiceBlockingStub.getUserDetails(request);
            UserDetails userDetails = new UserDetails();
            userDetails.setId(response.getId());
            userDetails.setEmail(response.getEmail());

            return userDetails;
        } catch (StatusRuntimeException e) {
            LOGGER.error("Cannot find user with id: {}", userId);
            return null;
        }
    }
}
