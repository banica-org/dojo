package com.dojo.notifications.service;

import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.model.contest.enums.CommonNotificationsLevel;
import com.dojo.notifications.model.contest.enums.EventType;
import com.dojo.notifications.model.contest.enums.NotifierType;
import com.dojo.notifications.model.leaderboard.Leaderboard;
import com.dojo.notifications.model.user.Participant;
import com.dojo.notifications.model.user.UserDetails;
import com.dojo.notifications.model.user.UserInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class LeaderboardNotifierServiceTest {

    private final String DUMMY_CONTEST_ID = "149";
    private final Participant FIRST_PARTICIPANT = new Participant(new UserInfo("1", "FirstUser"), 100);
    private final Participant SECOND_PARTICIPANT = new Participant(new UserInfo("2", "SecondUser"), 120);
    private final Leaderboard OLD_LEADERBOARD = new Leaderboard(Arrays.asList(FIRST_PARTICIPANT, SECOND_PARTICIPANT));
    private final Leaderboard NEW_LEADERBOARD = new Leaderboard(Arrays.asList(SECOND_PARTICIPANT, FIRST_PARTICIPANT));
    private final UserDetails FIRST_USER_DETAILS = new UserDetails();
    private final UserDetails SECOND_USER_DETAILS = new UserDetails();
    private final Map<NotifierType, CommonNotificationsLevel> leaderBoardNotificationsType = new ConcurrentHashMap<>();


    @Mock
    private LeaderboardService leaderboardService;

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

        leaderboardNotifierService = new LeaderboardNotifierService(userDetailsService, leaderboardService, Collections.singletonList(notificationService));

        Map<String, Leaderboard> leaderboards = new ConcurrentHashMap<>();
        leaderboards.put(DUMMY_CONTEST_ID, OLD_LEADERBOARD);
        ReflectionTestUtils.setField(leaderboardNotifierService, "leaderboards", leaderboards);

        leaderBoardNotificationsType.put(NotifierType.EMAIL, CommonNotificationsLevel.ON_ANY_LEADERBOARD_CHANGE);
    }

    @Test
    public void noChangesLeaderBoardTest() {

        //Act
        leaderboardNotifierService.lookForLeaderboardChanges(contest, OLD_LEADERBOARD);

        //Assert
        verify(contest, times(2)).getContestId();
    }

    @Test
    public void notifyPersonalChangesTest() {
        //Arrange
        when(leaderboardService.determineEventType(NEW_LEADERBOARD, OLD_LEADERBOARD)).thenReturn(EventType.POSITION_CHANGES);
        when(leaderboardService.getUserDetails(NEW_LEADERBOARD, OLD_LEADERBOARD)).thenReturn(Arrays.asList(FIRST_USER_DETAILS, SECOND_USER_DETAILS));
        when(contest.getPersonalNotifiers()).thenReturn(Collections.singleton(NotifierType.EMAIL));
        when(contest.getCommonNotificationsLevel()).thenReturn(leaderBoardNotificationsType);

        //Act
        leaderboardNotifierService.lookForLeaderboardChanges(contest, NEW_LEADERBOARD);

        //Assert
        verify(leaderboardService, times(1)).determineEventType(NEW_LEADERBOARD, OLD_LEADERBOARD);
        verify(leaderboardService, times(1)).getUserDetails(NEW_LEADERBOARD, OLD_LEADERBOARD);
        verify(contest, times(2)).getContestId();
        verify(contest, times(1)).getCommonNotificationsLevel();
        verify(contest, times(2)).getPersonalNotifiers();
        verify(notificationService, times(2)).notify(any(), any(), any());
        verify(notificationService, times(1)).notify(any(), any());
    }

    @Test
    public void notifyCommonChangesTest() {
        //Arrange
        when(leaderboardService.determineEventType(NEW_LEADERBOARD, OLD_LEADERBOARD)).thenReturn(EventType.SCORE_CHANGES);
        when(contest.getPersonalNotifiers()).thenReturn(Collections.singleton(NotifierType.EMAIL));
        when(contest.getCommonNotificationsLevel()).thenReturn(leaderBoardNotificationsType);

        //Act
        leaderboardNotifierService.lookForLeaderboardChanges(contest, NEW_LEADERBOARD);

        //Assert
        verify(leaderboardService, times(1)).determineEventType(NEW_LEADERBOARD, OLD_LEADERBOARD);
        verify(contest, times(2)).getContestId();
        verify(contest, times(1)).getCommonNotificationsLevel();
        verify(notificationService, times(1)).notify(any(), any());
    }

}
