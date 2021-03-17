package com.dojo.notifications.model.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private UserInfo user;
    private long score;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user1 = (User) o;
        return score == user1.score &&
                user.equals(user1.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, score);
    }
}
