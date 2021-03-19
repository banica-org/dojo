package com.dojo.notifications.service;


import com.dojo.notifications.configuration.Configuration;
import com.dojo.notifications.contest.Contest;
import com.dojo.notifications.contest.enums.EventType;
import com.dojo.notifications.model.user.User;
import com.dojo.notifications.model.user.UserDetails;
import com.dojo.notifications.model.user.UserInfo;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpMethod;


import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class LeaderboardServiceTest {

    private final String DUMMY_URL = "http://localhost:8081/api/v1/codenjoy/leaderboard";
    private final String CONTEST_ID = "149";
    private final User FIRST_USER = new User(new UserInfo(1, "FirstUser", "picture"), 100);
    private final User SECOND_USER = new User(new UserInfo(2, "SecondUser", "picture"), 120);
    private final List<User> OLD_LEADERBOARD = Arrays.asList(FIRST_USER, SECOND_USER);
    private final List<User> NEW_LEADERBOARD = Arrays.asList(SECOND_USER, FIRST_USER);
    private final UserDetails FIRST_USER_DETAILS = new UserDetails();
    private final UserDetails SECOND_USER_DETAILS = new UserDetails();


    @Mock
    private Configuration configuration;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private Contest contest;

    @InjectMocks
    private LeaderboardService leaderboardService;


    @Test
    public void getNewLeaderboardSetupTest() {
        //Arrange
        when(contest.getContestId()).thenReturn(CONTEST_ID);
        when(configuration.getLeaderboardApi()).thenReturn(DUMMY_URL);

        ParameterizedTypeReference<List<User>> obj = new ParameterizedTypeReference<List<User>>() {
        };
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(obj)))
                .thenReturn(new ResponseEntity<>(OLD_LEADERBOARD, HttpStatus.ACCEPTED));
        ReflectionTestUtils.setField(leaderboardService, "restTemplate", restTemplate);

        //Act
        List<User> actual = leaderboardService.getNewLeaderboardSetup(contest);

        //Assert
        Assert.assertEquals(OLD_LEADERBOARD, actual);

        verify(contest, times(1)).getContestId();
        verify(configuration, times(1)).getLeaderboardApi();
        verify(restTemplate, times(1)).exchange(anyString(), any(), any(), eq(obj));
    }

    @Test
    public void determineEventTypePositionChangeTest() {
        //Arrange

        //Act
        EventType actual = leaderboardService.determineEventType(NEW_LEADERBOARD, OLD_LEADERBOARD);

        //Assert
        Assert.assertEquals(EventType.POSITION_CHANGES, actual);
    }

    @Test
    public void determineEventTypeScoreChangeTest() {
        //Arrange
        User scoreChange = new User(new UserInfo(2, "SecondUser", "picture"), 420);
        List<User> newLeaderboard = Arrays.asList(FIRST_USER, scoreChange);

        //Act
        EventType actual = leaderboardService.determineEventType(newLeaderboard, OLD_LEADERBOARD);

        //Assert
        Assert.assertEquals(EventType.SCORE_CHANGES, actual);
    }

    @Test
    public void determineEventTypeOtherChangeTest() {
        //Arrange

        //Act
        EventType actual = leaderboardService.determineEventType(OLD_LEADERBOARD, OLD_LEADERBOARD);

        //Assert
        Assert.assertEquals(EventType.OTHER_LEADERBOARD_CHANGE, actual);
    }


    @Test
    public void getUserDetailsTest() {
        //Arrange
        FIRST_USER_DETAILS.setId(1);
        SECOND_USER_DETAILS.setId(2);
        List<UserDetails> expected = Arrays.asList(FIRST_USER_DETAILS, SECOND_USER_DETAILS);

        when(userDetailsService.getUserDetails(1)).thenReturn(FIRST_USER_DETAILS);
        when(userDetailsService.getUserDetails(2)).thenReturn(SECOND_USER_DETAILS);

        //Act
        List<UserDetails> actual = leaderboardService.getUserDetails(NEW_LEADERBOARD, OLD_LEADERBOARD);

        //Assert
        Assert.assertEquals(expected, actual);
        verify(userDetailsService, times(2)).getUserDetails(anyLong());
    }

}
