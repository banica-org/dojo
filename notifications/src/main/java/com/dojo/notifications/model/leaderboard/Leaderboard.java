package com.dojo.notifications.model.leaderboard;

import com.dojo.notifications.model.user.Participant;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Leaderboard {

    private final List<Participant> participants;

    public Leaderboard(List<Participant> participants) {
        this.participants = participants;
    }

    public int getParticipantsCount() {
        return this.participants.size();
    }

    public long getUserIdByPosition(int position) {
        return this.participants.get(position).getUser().getId();
    }

    public long getScoreByPosition(int position) {
        return this.participants.get(position).getScore();
    }

    public String getNameByPosition(int position) {
        return this.participants.get(position).getUser().getName();
    }

    public Participant getParticipantByPosition(int position) {
        return this.participants.get(position);
    }

    public List<Participant> getParticipants() {
        return Collections.unmodifiableList(this.participants);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Leaderboard)) return false;
        Leaderboard that = (Leaderboard) o;
        return participants.equals(that.participants);
    }

    @Override
    public int hashCode() {
        return Objects.hash(participants);
    }
}
