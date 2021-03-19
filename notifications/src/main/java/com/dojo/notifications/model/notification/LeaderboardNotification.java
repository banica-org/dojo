package com.dojo.notifications.model.notification;

import com.dojo.notifications.model.leaderboard.Leaderboard;

public abstract class LeaderboardNotification implements Notification {

    protected final Leaderboard leaderboard;

    public LeaderboardNotification(Leaderboard leaderboard) {
        this.leaderboard = leaderboard;
    }
}
