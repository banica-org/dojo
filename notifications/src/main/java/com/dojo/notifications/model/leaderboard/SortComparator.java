package com.dojo.notifications.model.leaderboard;

import com.dojo.notifications.model.user.Participant;

import java.util.Comparator;

public class SortComparator implements Comparator<Participant> {
    @Override
    public int compare(Participant p1, Participant p2) {
        return (int) (p2.getScore() - p1.getScore());
    }
}
