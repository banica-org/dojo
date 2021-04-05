package com.dojo.notifications.service;


import com.dojo.notifications.model.contest.enums.EventType;
import com.dojo.notifications.model.leaderboard.Leaderboard;
import com.dojo.notifications.model.user.Participant;
import com.dojo.notifications.model.user.UserDetails;
import com.dojo.notifications.model.user.UserInfo;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class LeaderboardServiceTest {

    private final Participant FIRST_PARTICIPANT = new Participant(new UserInfo("1", "FirstUser"), 100);
    private final Participant SECOND_PARTICIPANT = new Participant(new UserInfo("2", "SecondUser"), 120);
    private final Participant BETTER_FIRST_PARTICIPANT = new Participant(new UserInfo("1", "FirstUser"), 400);
    private final Leaderboard OLD_LEADERBOARD = new Leaderboard(Arrays.asList(FIRST_PARTICIPANT, SECOND_PARTICIPANT));
    private final Leaderboard POSITION_CHANGED_LEADERBOARD = new Leaderboard(Arrays.asList(SECOND_PARTICIPANT, BETTER_FIRST_PARTICIPANT));    private final UserDetails FIRST_USER_DETAILS = new UserDetails();
    private final UserDetails SECOND_USER_DETAILS = new UserDetails();

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private LeaderboardService leaderboardService;

    @Test
    public void determineEventTypePositionChangeTest() {


        //Act
        boolean actual = leaderboardService.determineEventType(POSITION_CHANGED_LEADERBOARD, OLD_LEADERBOARD, EventType.POSITION_CHANGES);

        //Assert
        Assert.assertTrue(actual);
    }

    @Test
    public void determineEventTypeScoreChangeTest() {
        //Arrange
        Participant scoreChange = new Participant(new UserInfo("2", "SecondUser"), 420);
        Leaderboard newLeaderboard = new Leaderboard(Arrays.asList(FIRST_PARTICIPANT, scoreChange));

        //Act
        boolean actual = leaderboardService.determineEventType(newLeaderboard, OLD_LEADERBOARD, EventType.SCORE_CHANGES);

        //Assert
        Assert.assertTrue(actual);
    }

    @Test
    public void determineEventTypeOtherChangeTest() {
        //Arrange

        //Act
        boolean actual = leaderboardService.determineEventType(OLD_LEADERBOARD, OLD_LEADERBOARD, EventType.OTHER_LEADERBOARD_CHANGE);

        //Assert
        Assert.assertTrue(actual);
    }


    @Test
    public void getUserDetailsTest() {
        //Arrange
        FIRST_USER_DETAILS.setId("1");
        SECOND_USER_DETAILS.setId("2");
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
