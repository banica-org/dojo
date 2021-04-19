package com.dojo.notifications.service;

import com.dojo.notifications.model.user.UserDetails;
import com.dojo.notifications.grpc.leaderboard.UserDetailsClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class UserDetailsServiceTest {

    private static final String USER_ID = "1";
    private static final String USER_EMAIL = "email@email";

    private UserDetails testUser;

    @Mock
    private UserDetailsClient userDetailsClient;

    private UserDetailsService userDetailsService;

    @Before
    public void init() {
        userDetailsService = new UserDetailsService(userDetailsClient);

        addUser();
    }

    @Test
    public void getUserDetailsTest() {
        UserDetails actual = userDetailsService.getUserDetails(USER_ID);

        assertEquals(testUser, actual);
    }

    @Test
    public void getUserEmailTest() {
        String actual = userDetailsService.getUserEmail(USER_ID);

        assertEquals(USER_EMAIL, actual);
    }

    private void addUser() {
        testUser = new UserDetails();
        testUser.setId(USER_ID);
        testUser.setEmail(USER_EMAIL);

        when(userDetailsClient.getUserDetailsById(USER_ID)).thenReturn(testUser);
    }

}
