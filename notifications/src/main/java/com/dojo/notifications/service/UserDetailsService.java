package com.dojo.notifications.service;

import com.dojo.notifications.model.user.UserDetails;
import com.dojo.notifications.service.grpc.UserDetailsClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

            userDetails = userDetailsClient.getUserDetails(userId);

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
}
