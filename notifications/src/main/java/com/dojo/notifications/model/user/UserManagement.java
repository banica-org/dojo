package com.dojo.notifications.model.user;

import com.dojo.notifications.grpc.UserDetailsClient;
import org.apache.flink.api.java.tuple.Tuple3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserManagement {

    private static final String USER = "ROLE_USER";

    private final Set<Tuple3<String, String, List<User>>> groups;
    private final UserDetailsClient userDetailsClient;

    @Autowired
    public UserManagement(Set<Tuple3<String, String, List<User>>> groups, UserDetailsClient userDetailsClient) {
        this.groups = groups;
        this.userDetailsClient = userDetailsClient;
    }

    public List<String> getAllAutocomplete(String contestId) {
        List<String> autocomplete = new ArrayList<>();
        autocomplete.addAll(getUsersForContest(contestId).stream()
                .map(user -> user.getId() + "." + user.getName())
                .collect(Collectors.toList()));

        autocomplete.addAll(getGroupNames(contestId).stream()
                .map(group -> group.f1)
                .collect(Collectors.toList()));
        return autocomplete;
    }

    public Set<Tuple3<String, String, List<User>>> getGroupNames(String contestId) {
        setParticipantsGroup(contestId);
        return groups.stream()
                .filter(group -> group.f0.equals(contestId))
                .collect(Collectors.toSet());
    }

    public List<User> getUsersForContest(String contestId) {
        return userDetailsClient.getUsersForContest(contestId);
    }

    public List<User> findUsersByGroupName(String name) {
        Optional<Tuple3<String, String, List<User>>> users = groups.stream()
                .filter(group -> group.f1.equals(name))
                .findFirst();
        return users.isPresent() ? users.get().f2 : Collections.emptyList();
    }

    private void setParticipantsGroup(String contestId) {
        List<User> participants = userDetailsClient.getUsersForContest(contestId).stream()
                .filter(user -> user.getRole().equals(USER))
                .collect(Collectors.toList());
        groups.add(new Tuple3<>(contestId, "All participants group", participants));
    }
}
