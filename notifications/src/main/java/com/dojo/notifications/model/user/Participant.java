package com.dojo.notifications.model.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Participant implements Comparable<Participant> {
    private UserInfo user;
    private long score;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Participant)) return false;
        Participant participant1 = (Participant) o;
        return score == participant1.score &&
                user.equals(participant1.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, score);
    }

    @Override
    public int compareTo(Participant o) {
        if(o.getScore() - this.getScore()==0){
            return o.getUser().getId().compareTo(this.getUser().getId());
        }
        return (int) (o.getScore() - this.getScore());
    }
}