package com.dojo.notifications.model.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@AllArgsConstructor
@Getter
@Setter
public class User {
    String id;
    String githubUserName;
    String name;
    String role;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return id.equals(user.id) &&
                githubUserName.equals(user.githubUserName) &&
                name.equals(user.name) &&
                role.equals(user.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, githubUserName, name, role);
    }


}
