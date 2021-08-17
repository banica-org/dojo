package com.dojo.notifications.model.user;

import com.dojo.notifications.grpc.UserDetailsClient;
import com.dojo.notifications.model.user.enums.UserRole;
import org.apache.flink.api.java.tuple.Tuple2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserManagement {

    private final HashMap<String, Tuple2<String, List<User>>> groups;
    private final UserDetailsClient userDetailsClient;

    @Autowired
    public UserManagement(HashMap<String, Tuple2<String, List<User>>> groups, UserDetailsClient userDetailsClient) {
        this.groups = groups;
        this.userDetailsClient = userDetailsClient;
    }

    public List<String> getAllAutocomplete(String contestId) {
        List<String> autocomplete = new ArrayList<>();
        autocomplete.addAll(getUsersForContest(contestId).stream()
                .map(user -> user.getId() + "." + user.getName())
                .collect(Collectors.toList()));

        autocomplete.addAll(getGroupNames(contestId).stream()
                .map(group -> group.f0)
                .collect(Collectors.toList()));
        return autocomplete;
    }

    public Set<Tuple2<String, List<User>>> getGroupNames(String contestId) {
        setUserGroups(contestId);
        return groups.entrySet().stream()
                .filter(group -> group.getKey().equals(contestId))
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());
    }

    public List<User> getUsersForContest(String contestId) {
        return userDetailsClient.getUsersForContest(contestId);
    }

    public List<User> findUsersByGroupName(String name) {
        Optional<Tuple2<String, List<User>>> users = groups.values().stream()
                .filter(group -> group.f0.equals(name))
                .findFirst();
        return users.isPresent() ? users.get().f1 : Collections.emptyList();
    }

    private void setUserGroups(String contestId) {
        for (UserRole role : UserRole.values()) {
            List<User> participants = userDetailsClient.getUsersForContest(contestId).stream()
                    .filter(user -> user.getRole().equals(role))
                    .collect(Collectors.toList());
            if (!participants.equals(Collections.emptyList())) {
                groups.put(contestId, new Tuple2<>("All " + role.toString().toLowerCase() + " group", participants));
            }
        }
    }
}
