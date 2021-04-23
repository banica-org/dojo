package com.dojo.notifications.grpc;

import com.codenjoy.dojo.ByIdRequest;
import com.codenjoy.dojo.ByUsernameRequest;
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


    public UserDetails getUserDetailsById(String userId) {
        ByIdRequest request = ByIdRequest.newBuilder().setId(userId).build();
        try {
            UserDetailsResponse response = userDetailsServiceBlockingStub.getUserDetailsById(request);
            return getUserDetails(response);
        } catch (StatusRuntimeException e) {
            LOGGER.error("Cannot find user with id: {}", userId);
            return null;
        }
    }

    public UserDetails getUserDetailsByUsername(String username) {
        ByUsernameRequest request = ByUsernameRequest.newBuilder().setUsername(username).build();
        try {
            UserDetailsResponse response = userDetailsServiceBlockingStub.getUserDetailsByUsername(request);
            return getUserDetails(response);
        } catch (StatusRuntimeException e) {
            LOGGER.error("Cannot find user with username: {}", username);
            return null;
        }
    }

    private UserDetails getUserDetails(UserDetailsResponse response) {
        UserDetails userDetails = new UserDetails();
        userDetails.setId(response.getId());
        userDetails.setEmail(response.getEmail());

        return userDetails;
    }
}
