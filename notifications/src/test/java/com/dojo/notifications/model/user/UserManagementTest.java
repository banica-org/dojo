package com.dojo.notifications.model.user;

import com.dojo.notifications.grpc.UserDetailsClient;
import com.dojo.notifications.model.user.enums.UserRole;
import org.apache.flink.api.java.tuple.Tuple2;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class UserManagementTest {

    private static final String CONTEST_ID = "kata";
    private static final String USER_ID = "1";
    private static final String USER_NAME = "username";
    private static final String USER_ROLE = "USER";

    private final HashMap<String, Tuple2<String, List<User>>> groups = new HashMap<>();

    @Mock
    private User user;

    @Mock
    private UserDetailsClient userDetailsClient;

    private UserManagement userManagement;

    @Before
    public void init() {
        userManagement = new UserManagement(groups, userDetailsClient);
    }

    @Test
    public void getAllAutocompleteTest() {
        when(userDetailsClient.getUsersForContest(CONTEST_ID)).thenReturn(Collections.singletonList(user));
        when(user.getId()).thenReturn(USER_ID);
        when(user.getName()).thenReturn(USER_NAME);
        when(user.getRole()).thenReturn(UserRole.valueOf(USER_ROLE));
        List<String> expected = Arrays.asList("1.username", "All user group");

        List<String> actual = userManagement.getAllAutocomplete(CONTEST_ID);

        Assert.assertTrue(actual.contains(expected.get(0)));
        Assert.assertTrue(actual.contains(expected.get(1)));

        verify(userDetailsClient, times(8)).getUsersForContest(CONTEST_ID);
        verify(user, times(1)).getId();
        verify(user, times(1)).getName();
        verify(user, times(7)).getRole();
    }

    @Test
    public void getGroupNamesTest() {
        when(user.getRole()).thenReturn(UserRole.valueOf(USER_ROLE));
        when(userDetailsClient.getUsersForContest(CONTEST_ID)).thenReturn(Collections.singletonList(user));

        Tuple2<String, List<User>> expected = new Tuple2<>("All user group", Collections.singletonList(user));

        Set<Tuple2<String, List<User>>> actual = userManagement.getGroupNames(CONTEST_ID);

        Assert.assertTrue(actual.stream().anyMatch(tuple -> tuple.equals(expected)));

        verify(userDetailsClient, times(7)).getUsersForContest(CONTEST_ID);
        verify(user, times(7)).getRole();
    }

    @Test
    public void getUsersForContestTest() {
        when(userDetailsClient.getUsersForContest(CONTEST_ID)).thenReturn(Collections.singletonList(user));
        List<User> expected = Collections.singletonList(user);

        List<User> actual = userManagement.getUsersForContest(CONTEST_ID);

        Assert.assertEquals(expected, actual);
        verify(userDetailsClient, times(1)).getUsersForContest(CONTEST_ID);
    }

    @Test
    public void findUsersByGroupNameTest() {
        groups.putIfAbsent(CONTEST_ID, new Tuple2<>(USER_NAME, Collections.singletonList(user)));

        List<User> actual = userManagement.findUsersByGroupName(USER_NAME);

        Assert.assertEquals(Collections.singletonList(user), actual);
    }

    @Test
    public void findUsersByGroupNameEmptyTest() {

        List<User> actual = userManagement.findUsersByGroupName(USER_NAME);

        Assert.assertEquals(Collections.emptyList(), actual);
    }
}
