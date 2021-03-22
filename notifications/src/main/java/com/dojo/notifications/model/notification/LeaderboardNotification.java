package com.dojo.notifications.model.notification;

import com.dojo.notifications.model.leaderboard.Leaderboard;
import com.dojo.notifications.service.UserDetailsService;

public abstract class LeaderboardNotification implements Notification {

    protected final UserDetailsService userDetailsService;
    protected final Leaderboard leaderboard;

    public LeaderboardNotification(UserDetailsService userDetailsService, Leaderboard leaderboard) {
        this.userDetailsService = userDetailsService;
        this.leaderboard = leaderboard;
    }
}
