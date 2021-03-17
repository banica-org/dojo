package com.dojo.notifications.service;

import com.dojo.notifications.configuration.Configuration;
import com.dojo.notifications.contest.Contest;
import com.dojo.notifications.contest.enums.CommonNotificationsLevel;
import com.dojo.notifications.contest.enums.EventType;
import com.dojo.notifications.contest.enums.NotifierType;
import com.dojo.notifications.model.user.User;
import com.dojo.notifications.model.user.UserDetails;
import com.dojo.notifications.model.user.UserInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class LeaderboardNotifierServiceTest {

    private final String DUMMY_URL = "http://localhost:8081/api/v1/codenjoy/leaderboard";
    private final String DUMMY_CONTEST_ID = "149";
    private final User FIRST_USER = new User(new UserInfo(1, "FirstUser", "picture"), 100);
    private final User SECOND_USER = new User(new UserInfo(2, "SecondUser", "picture"), 120);
    private final List<User> OLD_LEADERBOARD = Arrays.asList(FIRST_USER, SECOND_USER);
    private final List<User> NEW_LEADERBOARD = Arrays.asList(SECOND_USER, FIRST_USER);
    private final UserDetails FIRST_USER_DETAILS = new UserDetails();
    private final UserDetails SECOND_USER_DETAILS = new UserDetails();
    private final Map<NotifierType, CommonNotificationsLevel> leaderBoardNotificationsType = new ConcurrentHashMap<>();


    @Mock
    private Configuration configuration;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private Contest contest;

    @Mock
    private NotificationService notificationService;

    private LeaderboardNotifierService leaderboardNotifierService;

    @Before
    public void init() {
        when(contest.getContestId()).thenReturn(DUMMY_CONTEST_ID);
        when(notificationService.getNotificationServiceTypeMapping()).thenReturn(NotifierType.EMAIL);

        leaderboardNotifierService = new LeaderboardNotifierService(configuration, Collections.singletonList(notificationService), userDetailsService);


        Map<String, List<User>> leaderboards = new ConcurrentHashMap<>();
        leaderboards.put("149", OLD_LEADERBOARD);
        ReflectionTestUtils.setField(leaderboardNotifierService, "leaderboards", leaderboards);

        leaderBoardNotificationsType.put(NotifierType.EMAIL, CommonNotificationsLevel.ON_ANY_LEADERBOARD_CHANGE);
    }

    @Test
    public void getNoChangesLeaderBoardTest() {
        //Arrange
        when(configuration.getLeaderboardApi()).thenReturn(DUMMY_URL);

        ParameterizedTypeReference<List<User>> obj = new ParameterizedTypeReference<List<User>>() {
        };
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(obj)))
                .thenReturn(new ResponseEntity<>(OLD_LEADERBOARD, HttpStatus.ACCEPTED));
        ReflectionTestUtils.setField(leaderboardNotifierService, "restTemplate", restTemplate);

        //Act
        leaderboardNotifierService.getLeaderBoard(contest);

        //Assert
        verify(configuration, times(1)).getLeaderboardApi();
        verify(contest, times(3)).getContestId();
        verify(restTemplate, times(1)).exchange(anyString(), any(), any(), eq(obj));
    }


    @Test
    public void getPositionChangesEventTypeTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        //Arrange

        //Act
        Method method = LeaderboardNotifierService.class.getDeclaredMethod("determineEventType", List.class, Contest.class);
        method.setAccessible(true);

        EventType expected = EventType.POSITION_CHANGES;
        EventType actual = (EventType) method.invoke(leaderboardNotifierService, NEW_LEADERBOARD, contest);

        //Assert
        Assert.assertEquals(expected, actual);
        verify(contest, times(1)).getContestId();
    }

    @Test
    public void getScoreChangesEventTypeTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        //Arrange
        User scoreChange = new User(new UserInfo(2, "SecondUser", "picture"), 420);
        List<User> newLeaderboard = Arrays.asList(FIRST_USER, scoreChange);

        //Act
        Method method = LeaderboardNotifierService.class.getDeclaredMethod("determineEventType", List.class, Contest.class);
        method.setAccessible(true);

        EventType expected = EventType.SCORE_CHANGES;
        EventType actual = (EventType) method.invoke(leaderboardNotifierService, newLeaderboard, contest);

        //Assert
        Assert.assertEquals(expected, actual);
        verify(contest, times(1)).getContestId();
    }

    @Test
    public void getOtherLeaderBoardChangeEventTypeTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        //Arrange

        //Act
        Method method = LeaderboardNotifierService.class.getDeclaredMethod("determineEventType", List.class, Contest.class);
        method.setAccessible(true);

        EventType expected = EventType.OTHER_LEADERBOARD_CHANGE;
        EventType actual = (EventType) method.invoke(leaderboardNotifierService, OLD_LEADERBOARD, contest);

        //Assert
        Assert.assertEquals(expected, actual);
        verify(contest, times(1)).getContestId();
    }


    @Test
    public void notifyPersonalChangesTest() {
        //Arrange
        FIRST_USER_DETAILS.setId(1);
        SECOND_USER_DETAILS.setId(2);

        when(contest.getCommonNotificationsLevel()).thenReturn(leaderBoardNotificationsType);
        when(contest.getPersonalNotifiers()).thenReturn(Collections.singleton(NotifierType.EMAIL));
        when(userDetailsService.getUserDetails(1)).thenReturn(FIRST_USER_DETAILS);
        when(userDetailsService.getUserDetails(2)).thenReturn(SECOND_USER_DETAILS);

        //Act
        leaderboardNotifierService.notifyAboutChanges(contest, NEW_LEADERBOARD);

        //Assert
        verify(contest, times(2)).getContestId();
        verify(contest, times(1)).getCommonNotificationsLevel();
        verify(contest, times(2)).getPersonalNotifiers();
        verify(userDetailsService, times(2)).getUserDetails(anyLong());
        verify(notificationService, times(2)).notify(any(), any(), any());
    }

    @Test
    public void notifyAndApplyOtherLeaderboardChangesTest2() {
        //Arrange
        when(contest.getCommonNotificationsLevel()).thenReturn(leaderBoardNotificationsType);

        //Act
        leaderboardNotifierService.notifyAboutChanges(contest, OLD_LEADERBOARD);

        //Assert
        verify(contest, times(1)).getContestId();
        verify(contest, times(1)).getCommonNotificationsLevel();
        verify(notificationService, times(1)).notify(any(), any());
    }

}
