package com.dojo.notifications.grpc;

import com.codenjoy.dojo.UserDetailsIdRequest;
import com.codenjoy.dojo.UserDetailsResponse;
import com.codenjoy.dojo.UserDetailsServiceGrpc;
import com.codenjoy.dojo.UserDetailsUsernameRequest;
import com.codenjoy.dojo.UserRequest;
import com.codenjoy.dojo.UserResponse;
import com.dojo.notifications.model.user.User;
import com.dojo.notifications.model.user.UserDetails;
import com.dojo.notifications.model.user.enums.UserRole;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserDetailsClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDetailsClient.class);

    private final UserDetailsServiceGrpc.UserDetailsServiceBlockingStub userDetailsServiceBlockingStub;

    @Autowired
    public UserDetailsClient(UserDetailsServiceGrpc.UserDetailsServiceBlockingStub userDetailsServiceBlockingStub) {
        this.userDetailsServiceBlockingStub = userDetailsServiceBlockingStub;
    }


    public UserDetails getUserDetailsById(String userId) {
        UserDetailsIdRequest request = UserDetailsIdRequest.newBuilder().setId(userId).build();
        try {
            UserDetailsResponse response = userDetailsServiceBlockingStub.getUserDetailsById(request);
            return getUserDetails(response);
        } catch (StatusRuntimeException e) {
            LOGGER.error("Cannot find user with id: {}", userId);
            return null;
        }
    }

    public UserDetails getUserDetailsByUsername(String username) {
        UserDetailsUsernameRequest request = UserDetailsUsernameRequest.newBuilder().setUsername(username).build();
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
        userDetails.setSlackEmail(response.getSlackEmail());
        userDetails.setSubscribed(response.getSubscribed());

        System.out.println(userDetails.getId() + userDetails.getEmail() + userDetails.getSlackEmail() + userDetails.isSubscribed());

        return userDetails;
    }

    public List<User> getUsersForContest(String contestId){
        UserRequest userRequest = UserRequest.newBuilder()
                .setContestId(contestId).build();
        UserResponse userResponse = userDetailsServiceBlockingStub.getUsersForContest(userRequest);

        return userResponse.getUserList().stream().map(user -> {
            return new User(user.getId(),user.getUsername(),user.getName(), UserRole.valueOf(user.getRole().replace("ROLE_","")));
        }).collect(Collectors.toList());
    }
}
