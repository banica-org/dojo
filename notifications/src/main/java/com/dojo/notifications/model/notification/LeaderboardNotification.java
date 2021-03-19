package com.dojo.notifications.model.notification;

import com.dojo.notifications.model.leaderboard.Leaderboard;
import com.dojo.notifications.service.UserDetailsService;

public abstract class LeaderboardNotification implements Notification {

    protected final Leaderboard leaderboard;
    protected final UserDetailsService userDetailsService;

    public LeaderboardNotification(Leaderboard leaderboard, UserDetailsService userDetailsService) {
        this.leaderboard = leaderboard;
        this.userDetailsService = userDetailsService;
    }
}
