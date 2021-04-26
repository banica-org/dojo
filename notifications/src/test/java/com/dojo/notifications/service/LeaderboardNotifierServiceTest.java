package com.dojo.notifications.service;

import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.model.contest.enums.CommonNotificationsLevel;
import com.dojo.notifications.model.contest.enums.NotifierType;
import com.dojo.notifications.model.leaderboard.Leaderboard;
import com.dojo.notifications.model.request.SelectRequest;
import com.dojo.notifications.model.user.Participant;
import com.dojo.notifications.model.user.UserDetails;
import com.dojo.notifications.model.user.UserInfo;
import com.dojo.notifications.service.notificationService.NotificationService;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class LeaderboardNotifierServiceTest {

    private final String DUMMY_CONTEST_ID = "149";
    private final String ANOTHER_CONTEST_ID = "153";
    private final Participant FIRST_PARTICIPANT = new Participant(new UserInfo("1", "FirstUser"), 100);
    private final Participant SECOND_PARTICIPANT = new Participant(new UserInfo("2", "SecondUser"), 120);
    private final Participant THIRD_PARTICIPANT = new Participant(new UserInfo("3", "ThirdUser"), 400);
    private final Participant UPDATED_PARTICIPANT = new Participant(new UserInfo("3", "ThirdUser"), 800);


    private final Leaderboard OLD_LEADERBOARD = new Leaderboard(new TreeSet<>());
    private final Leaderboard NEW_LEADERBOARD = new Leaderboard(new TreeSet<>());//Arrays.asList(SECOND_PARTICIPANT, FIRST_PARTICIPANT, THIRD_PARTICIPANT));

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

    Map<String, Leaderboard> leaderboards;

    private LeaderboardNotifierService leaderboardNotifierService;

    @Before
    public void init() {
        when(contest.getContestId()).thenReturn(DUMMY_CONTEST_ID);
        when(notificationService.getNotificationServiceTypeMapping()).thenReturn(NotifierType.EMAIL);

        OLD_LEADERBOARD.getParticipants().addAll(Arrays.asList(FIRST_PARTICIPANT, SECOND_PARTICIPANT));
        NEW_LEADERBOARD.getParticipants().addAll(Arrays.asList(SECOND_PARTICIPANT, FIRST_PARTICIPANT, THIRD_PARTICIPANT));

        leaderboardNotifierService = new LeaderboardNotifierService(userDetailsService, leaderboardService, selectRequestService, flinkTableService, Collections.singletonList(notificationService));

        leaderboards = new ConcurrentHashMap<>();
        leaderboards.put(DUMMY_CONTEST_ID, OLD_LEADERBOARD);
        ReflectionTestUtils.setField(leaderboardNotifierService, "leaderboards", leaderboards);

        leaderBoardNotificationsType.put(NotifierType.EMAIL, CommonNotificationsLevel.ON_ANY_LEADERBOARD_CHANGE);
    }

    @Test
    public void isBoardReceivedTest() {
        assertTrue(leaderboardNotifierService.isLeaderboardReceived(DUMMY_CONTEST_ID));
    }

    @Test
    public void getLeaderboardOnStartTest() {
        leaderboardNotifierService.setLeaderboardOnStart(ANOTHER_CONTEST_ID, OLD_LEADERBOARD);
        assertEquals(2, leaderboards.size());
    }

    @Test
    public void getLeaderboardUpdateTest() {
        leaderboardNotifierService.updateLeaderboard(contest, THIRD_PARTICIPANT);
        assertEquals(400, leaderboards.get(DUMMY_CONTEST_ID).getScoreByPosition(0));

        leaderboardNotifierService.updateLeaderboard(contest, UPDATED_PARTICIPANT);
        assertEquals(800, leaderboards.get(DUMMY_CONTEST_ID).getScoreByPosition(0));
    }

    @Test
    public void noChangesLeaderBoardTest() {

        //Act
        leaderboardNotifierService.lookForLeaderboardChanges(contest, OLD_LEADERBOARD);

        //Assert
        verify(contest, times(2)).getContestId();
    }

    @Test
    public void notifyPersonalChangesTest() throws Exception {
        //Arrange
        SELECT_REQUEST.setEventType("POSITION_CHANGES");
        SELECT_REQUEST.setReceiver("Participant");
        SELECT_REQUEST.setCondition(1);
        List<SelectRequest> requests = new ArrayList<>(Collections.singletonList(SELECT_REQUEST));

        when(selectRequestService.getRequests()).thenReturn(requests);
        when(flinkTableService.executeSingleQuery(any(), eq(NEW_LEADERBOARD), eq(OLD_LEADERBOARD))).thenReturn(NEW_LEADERBOARD.getParticipants());
        when(userDetailsService.getUserDetails(NEW_LEADERBOARD.getUserIdByPosition(0))).thenReturn(FIRST_USER_DETAILS);
        when(userDetailsService.getUserDetails(NEW_LEADERBOARD.getUserIdByPosition(1))).thenReturn(SECOND_USER_DETAILS);
        when(contest.getPersonalNotifiers()).thenReturn(Collections.singleton(NotifierType.EMAIL));

        //Act
        leaderboardNotifierService.lookForLeaderboardChanges(contest, NEW_LEADERBOARD);

        //Assert
        verify(selectRequestService, times(1)).getRequests();
        verify(flinkTableService, times(1)).executeSingleQuery(any(), eq(NEW_LEADERBOARD), eq(OLD_LEADERBOARD));
        verify(userDetailsService, times(3)).getUserDetails(any());
        verify(contest, times(2)).getContestId();
        verify(contest, times(3)).getPersonalNotifiers();
        verify(notificationService, times(3)).notify(any(), any(), any());
    }

    @Test
    public void notifyCommonChangesTest() throws Exception {
        //Arrange
        SELECT_REQUEST.setEventType("SCORE_CHANGES");
        SELECT_REQUEST.setReceiver("All");
        SELECT_REQUEST.setCondition(1);
        List<SelectRequest> requests = new ArrayList<>(Collections.singletonList(SELECT_REQUEST));

        when(selectRequestService.getRequests()).thenReturn(requests);
        when(flinkTableService.executeSingleQuery(any(), eq(NEW_LEADERBOARD), eq(OLD_LEADERBOARD))).thenReturn(NEW_LEADERBOARD.getParticipants());
        when(contest.getPersonalNotifiers()).thenReturn(Collections.singleton(NotifierType.EMAIL));
        when(contest.getCommonNotificationsLevel()).thenReturn(leaderBoardNotificationsType);

        //Act
        leaderboardNotifierService.lookForLeaderboardChanges(contest, NEW_LEADERBOARD);

        //Assert
        verify(selectRequestService, times(1)).getRequests();
        verify(flinkTableService, times(1)).executeSingleQuery(any(), eq(NEW_LEADERBOARD), eq(OLD_LEADERBOARD));
        verify(contest, times(2)).getContestId();
        verify(contest, times(1)).getCommonNotificationsLevel();
        verify(notificationService, times(1)).notify(any(), any());
    }

}
