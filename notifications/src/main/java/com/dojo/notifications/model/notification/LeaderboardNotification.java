package com.dojo.notifications.model.notification;

import com.dojo.notifications.model.user.User;
import com.dojo.notifications.service.UserDetailsService;

import java.util.List;

public abstract class LeaderboardNotification implements Notification {

    protected final List<User> leaderboard;

    private final UserDetailsService userDetailsService;

    public LeaderboardNotification(List<User> leaderboard, UserDetailsService userDetailsService) {
        this.leaderboard = leaderboard;
        this.userDetailsService = userDetailsService;
    }
}
