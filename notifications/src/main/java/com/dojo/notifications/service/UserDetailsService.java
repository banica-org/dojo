package com.dojo.notifications.service;

import com.dojo.notifications.model.user.User;
import com.dojo.notifications.model.user.UserDetails;
import com.dojo.notifications.grpc.UserDetailsClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class UserDetailsService {

    private final UserDetailsClient userDetailsClient;
    private final Map<String, UserDetails> userDetailsCache;

    @Autowired
    public UserDetailsService(UserDetailsClient userDetailsClient) {
        this.userDetailsClient = userDetailsClient;
        this.userDetailsCache = new ConcurrentHashMap<>();
    }

    public UserDetails getUserDetails(String userId) {
        UserDetails userDetails = userDetailsCache.get(userId);
        if (userDetails == null) {

            userDetails = userDetailsClient.getUserDetailsById(userId);

            if (userDetails != null) {
                userDetailsCache.put(userId, userDetails);
            }
        }
        return userDetails;
    }

    public String getUserEmail(String userId) {
        UserDetails userDetails = getUserDetails(userId);
        return userDetails != null ? userDetails.getEmail() : null;
    }

    public UserDetails getUserDetailsByUsername(String username) {
        UserDetails userDetails = userDetailsClient.getUserDetailsByUsername(username);
        if (userDetails != null) {
            userDetailsCache.put(userDetails.getId(), userDetails);
        }
        return userDetails;
    }

    public List<String> getUserNames(String contestId){
        List<User> userList = userDetailsClient.getUsersForContest(contestId);
        return userList.stream()
                .map(User::getGithubUserName)
                .collect(Collectors.toList());
    }

    public List<String> getNames(String contestId){
        List<User> userList = userDetailsClient.getUsersForContest(contestId);
        return userList.stream()
                .map(User::getName)
                .collect(Collectors.toList());
    }
}
