package com.dojo.notifications.service;

import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.model.contest.enums.CommonNotificationsLevel;
import com.dojo.notifications.model.contest.enums.EventType;
import com.dojo.notifications.model.contest.enums.NotifierType;
import com.dojo.notifications.model.leaderboard.Leaderboard;
import com.dojo.notifications.model.leaderboard.SortComparator;
import com.dojo.notifications.model.request.SelectRequest;
import com.dojo.notifications.model.user.Participant;
import com.dojo.notifications.model.user.UserDetails;
import com.dojo.notifications.model.user.UserInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class LeaderboardNotifierServiceTest {

    private final String DUMMY_CONTEST_ID = "149";
    private final Participant FIRST_PARTICIPANT = new Participant(new UserInfo("1", "FirstUser"), 100);
    private final Participant SECOND_PARTICIPANT = new Participant(new UserInfo("2", "SecondUser"), 120);
    private final Participant THIRD_PARTICIPANT = new Participant(new UserInfo("3", "ThirdUser"), 400);

    private final Leaderboard OLD_LEADERBOARD = new Leaderboard(new TreeSet<>(new SortComparator()));
    private final Leaderboard NEW_LEADERBOARD = new Leaderboard(new TreeSet<>(new SortComparator()));//Arrays.asList(SECOND_PARTICIPANT, FIRST_PARTICIPANT, THIRD_PARTICIPANT));

    private final UserDetails FIRST_USER_DETAILS = new UserDetails();
    private final UserDetails SECOND_USER_DETAILS = new UserDetails();

    private final SelectRequest SELECT_REQUEST = new SelectRequest();
    private final Map<NotifierType, CommonNotificationsLevel> leaderBoardNotificationsType = new ConcurrentHashMap<>();

    @Mock
    private FlinkTableService flinkTableService;

    @Mock
    private SelectRequestService selectRequestService;

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

        OLD_LEADERBOARD.getParticipants().addAll(Arrays.asList(FIRST_PARTICIPANT,SECOND_PARTICIPANT));
        NEW_LEADERBOARD.getParticipants().addAll(Arrays.asList(SECOND_PARTICIPANT,FIRST_PARTICIPANT,THIRD_PARTICIPANT));

        leaderboardNotifierService = new LeaderboardNotifierService(userDetailsService, leaderboardService, selectRequestService, flinkTableService, Collections.singletonList(notificationService));

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
        SELECT_REQUEST.setEventType("POSITION_CHANGES");
        List<SelectRequest> requests = new ArrayList<>(Collections.singletonList(SELECT_REQUEST));

        when(selectRequestService.getRequests()).thenReturn(requests);
        when(flinkTableService.executeSingleQuery(any(), eq("NewLeaderboard"), any())).thenReturn(NEW_LEADERBOARD);
        when(flinkTableService.executeSingleQuery(any(), eq("OldLeaderboard"), any())).thenReturn(OLD_LEADERBOARD);
        when(leaderboardService.isItTheWantedEventType(any(), any(), eq(EventType.POSITION_CHANGES))).thenReturn(true);
        when(leaderboardService.getUserDetails(any(), any())).thenReturn(Arrays.asList(FIRST_USER_DETAILS, SECOND_USER_DETAILS));
        when(contest.getPersonalNotifiers()).thenReturn(Collections.singleton(NotifierType.EMAIL));
        when(contest.getCommonNotificationsLevel()).thenReturn(leaderBoardNotificationsType);

        //Act
        leaderboardNotifierService.lookForLeaderboardChanges(contest, NEW_LEADERBOARD);

        //Assert
        verify(selectRequestService, times(1)).getRequests();
        verify(flinkTableService, times(1)).executeSingleQuery(any(), eq("NewLeaderboard"), any());
        verify(flinkTableService, times(1)).executeSingleQuery(any(), eq("OldLeaderboard"), any());
        verify(leaderboardService, times(1)).isItTheWantedEventType(any(), any(), eq(EventType.POSITION_CHANGES));
        verify(leaderboardService, times(1)).getUserDetails(any(), any());
        verify(contest, times(2)).getContestId();
        verify(contest, times(1)).getCommonNotificationsLevel();
        verify(contest, times(2)).getPersonalNotifiers();
        verify(notificationService, times(2)).notify(any(), any(), any());
        verify(notificationService, times(1)).notify(any(), any());
    }

    @Test
    public void notifyCommonChangesTest() {
        //Arrange
        SELECT_REQUEST.setEventType("SCORE_CHANGES");
        List<SelectRequest> requests = new ArrayList<>(Collections.singletonList(SELECT_REQUEST));

        when(selectRequestService.getRequests()).thenReturn(requests);
        when(flinkTableService.executeSingleQuery(any(), eq("NewLeaderboard"), any())).thenReturn(NEW_LEADERBOARD);
        when(flinkTableService.executeSingleQuery(any(), eq("OldLeaderboard"), any())).thenReturn(OLD_LEADERBOARD);
        when(leaderboardService.isItTheWantedEventType(any(), any(), eq(EventType.SCORE_CHANGES))).thenReturn(true);
        when(contest.getPersonalNotifiers()).thenReturn(Collections.singleton(NotifierType.EMAIL));
        when(contest.getCommonNotificationsLevel()).thenReturn(leaderBoardNotificationsType);

        //Act
        leaderboardNotifierService.lookForLeaderboardChanges(contest, NEW_LEADERBOARD);

        //Assert
        verify(selectRequestService, times(1)).getRequests();
        verify(flinkTableService, times(1)).executeSingleQuery(any(), eq("NewLeaderboard"), any());
        verify(flinkTableService, times(1)).executeSingleQuery(any(), eq("OldLeaderboard"), any());
        verify(leaderboardService, times(1)).isItTheWantedEventType(any(), any(), eq(EventType.SCORE_CHANGES));
        verify(contest, times(2)).getContestId();
        verify(contest, times(1)).getCommonNotificationsLevel();
        verify(notificationService, times(1)).notify(any(), any());
    }

}
