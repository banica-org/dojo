package com.dojo.notifications.service;

import com.dojo.notifications.model.user.User;
import com.dojo.notifications.model.user.UserDetails;
import com.dojo.notifications.grpc.UserDetailsClient;
import com.dojo.notifications.model.user.UserManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class UserDetailsService {

    private final UserDetailsClient userDetailsClient;
    private final Map<String, UserDetails> userDetailsCache;
    private final UserManagement userManagement;


    @Autowired
    public UserDetailsService(UserDetailsClient userDetailsClient, UserManagement userManagement) {
        this.userDetailsClient = userDetailsClient;
        this.userManagement = userManagement;
        this.userDetailsCache = new ConcurrentHashMap<>();
    }

    public UserDetails getUserDetailsById(String userId) {
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
        UserDetails userDetails = getUserDetailsById(userId);
        return userDetails != null ? userDetails.getEmail() : null;
    }

    public UserDetails getUserDetailsByUsername(String username) {
        UserDetails userDetails = userDetailsClient.getUserDetailsByUsername(username);
        if (userDetails != null) {
            userDetailsCache.put(userDetails.getId(), userDetails);
        }
        return userDetails;
    }

    public List<String> getUserNames(String contestId) {
        List<User> userList = userDetailsClient.getUsersForContest(contestId);
        return userList.stream()
                .map(User::getGithubUserName)
                .collect(Collectors.toList());
    }

    public Set<String> turnUsersToUserIds(String users) {
        List<User> group = new ArrayList<>();
        Set<String> userIds = new TreeSet<>();
        Arrays.stream(users.split(","))
                .forEach(entry -> {
                    if (isParticipant(entry.split("\\.")[0])) {
                        userIds.add(entry.split("\\.")[0]);
                    } else {
                        group.addAll(userManagement.findUsersByGroupName(entry));
                    }
                });
        group.forEach(user -> userIds.add(user.getId()));
        return userIds;
    }

    private boolean isParticipant(String id) {
        return getUserDetailsById(id) != null;
    }
}
