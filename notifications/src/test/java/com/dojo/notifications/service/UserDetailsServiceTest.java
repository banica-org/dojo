package com.dojo.notifications.service;

import com.dojo.notifications.model.user.User;
import com.dojo.notifications.model.user.UserDetails;
import com.dojo.notifications.grpc.UserDetailsClient;
import com.dojo.notifications.model.user.UserManagement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class UserDetailsServiceTest {

    private static final String USER_ID = "1";
    private static final String USER_EMAIL = "email@email";
    private static final String USERNAME = "username";
    private static final String CONTEST_ID = "kata";

    private UserDetails testUser;

    @Mock
    private UserManagement userManagement;

    @Mock
    private UserDetailsClient userDetailsClient;

    @Mock
    private User user;

    private UserDetailsService userDetailsService;

    @Before
    public void init() {
        userDetailsService = new UserDetailsService(userDetailsClient, userManagement);

        addUser();
    }

    @Test
    public void getUserDetailsByIdTest() {
        UserDetails actual = userDetailsService.getUserDetailsById(USER_ID);

        assertEquals(testUser, actual);
    }

    @Test
    public void getUserEmailTest() {
        String actual = userDetailsService.getUserEmail(USER_ID);

        assertEquals(USER_EMAIL, actual);
    }

    @Test
    public void getUserDetailsByUsernameTest() {
        UserDetails actual = userDetailsService.getUserDetailsByUsername(USERNAME);
        assertEquals(testUser, actual);
    }

    @Test
    public void getUserNamesTest() {
        List<String> expected = Collections.singletonList(USERNAME);
        when(userDetailsClient.getUsersForContest(CONTEST_ID)).thenReturn(Collections.singletonList(user));
        when(user.getGithubUserName()).thenReturn(USERNAME);

        List<String> actual = userDetailsService.getUserNames(CONTEST_ID);

        assertEquals(expected, actual);
        verify(userDetailsClient, times(1)).getUsersForContest(CONTEST_ID);
        verify(user, times(1)).getGithubUserName();
    }

    private void addUser() {
        testUser = new UserDetails();
        testUser.setId(USER_ID);
        testUser.setEmail(USER_EMAIL);

        when(userDetailsClient.getUserDetailsById(USER_ID)).thenReturn(testUser);
        when(userDetailsClient.getUserDetailsByUsername(USERNAME)).thenReturn(testUser);
    }

    @Test
    public void turnUsersToUserIdsTest() {

        Set<String> actual = userDetailsService.turnUsersToUserIds(USER_ID + "." + USERNAME);

        assertEquals(Collections.singleton(USER_ID), actual);
    }

    @Test
    public void turnGroupUsersToUserIdsTest() {
        when(userManagement.findUsersByGroupName(USERNAME)).thenReturn(Collections.singletonList(user));
        when(user.getId()).thenReturn(USER_ID);

        Set<String> actual = userDetailsService.turnUsersToUserIds(USERNAME);

        assertEquals(Collections.singleton(USER_ID), actual);
        verify(userManagement, times(1)).findUsersByGroupName(USERNAME);
        verify(user, times(1)).getId();
    }

}
