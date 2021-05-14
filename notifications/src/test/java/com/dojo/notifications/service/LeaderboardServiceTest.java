package com.dojo.notifications.service;

import com.dojo.notifications.model.leaderboard.Leaderboard;
import com.dojo.notifications.model.user.Participant;
import com.dojo.notifications.model.user.UserDetails;
import com.dojo.notifications.model.user.UserInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class LeaderboardServiceTest {

    private final Participant FIRST_PARTICIPANT = new Participant(new UserInfo("1", "FirstUser"), 100);
    private final Participant SECOND_PARTICIPANT = new Participant(new UserInfo("2", "SecondUser"), 120);
    private final Participant BETTER_FIRST_PARTICIPANT = new Participant(new UserInfo("1", "FirstUser"), 400);
    private final Leaderboard OLD_LEADERBOARD = new Leaderboard(new TreeSet<>());
    private final Leaderboard POSITION_CHANGED_LEADERBOARD = new Leaderboard(new TreeSet<>());
    private final UserDetails FIRST_USER_DETAILS = new UserDetails();
    private final UserDetails SECOND_USER_DETAILS = new UserDetails();

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private LeaderboardService leaderboardService;

    @Before
    public void init() {
        OLD_LEADERBOARD.getParticipants().addAll(Arrays.asList(FIRST_PARTICIPANT, SECOND_PARTICIPANT));
        FIRST_USER_DETAILS.setId("1");
        SECOND_USER_DETAILS.setId("2");
        POSITION_CHANGED_LEADERBOARD.getParticipants().addAll(Arrays.asList(SECOND_PARTICIPANT, BETTER_FIRST_PARTICIPANT));
    }

    @Test
    public void getUserDetailsTest() {
        //Arrange
        List<UserDetails> expected = Arrays.asList(FIRST_USER_DETAILS, SECOND_USER_DETAILS);

        when(userDetailsService.getUserDetails(OLD_LEADERBOARD.getUserIdByPosition(0))).thenReturn(FIRST_USER_DETAILS);
        when(userDetailsService.getUserDetails(OLD_LEADERBOARD.getUserIdByPosition(1))).thenReturn(SECOND_USER_DETAILS);

        //Act
        List<UserDetails> actual = leaderboardService.getUserDetails(POSITION_CHANGED_LEADERBOARD, OLD_LEADERBOARD);

        //Assert
        Assert.assertEquals(expected, actual);
        verify(userDetailsService, times(2)).getUserDetails(any());
    }

}
