package com.dojo.notifications.service.notifierService;

import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.model.contest.enums.NotifierType;
import com.dojo.notifications.model.leaderboard.Leaderboard;
import com.dojo.notifications.model.request.SelectRequest;
import com.dojo.notifications.model.user.Participant;
import com.dojo.notifications.model.user.UserDetails;
import com.dojo.notifications.model.user.UserInfo;
import com.dojo.notifications.service.FlinkTableService;
import com.dojo.notifications.service.LeaderboardService;
import com.dojo.notifications.service.SelectRequestService;
import com.dojo.notifications.service.UserDetailsService;
import com.dojo.notifications.service.notificationService.NotificationService;
import org.apache.flink.api.java.tuple.Tuple4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private static final String USER_ID = "userid";

    private static final String TABLE_NAME = "leaderboard";

    private final Leaderboard OLD_LEADERBOARD = new Leaderboard(new TreeSet<>());
    private final Leaderboard NEW_LEADERBOARD = new Leaderboard(new TreeSet<>());
    private final List<Tuple4<String, String, Integer, Long>> CHANGED_USERS = new ArrayList<>();

    private final UserDetails FIRST_USER_DETAILS = new UserDetails();
    private final UserDetails SECOND_USER_DETAILS = new UserDetails();

    private final SelectRequest SELECT_REQUEST = new SelectRequest();
    private final Set<NotifierType> notifiers = new HashSet<>();

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

    private Map<String, Leaderboard> leaderboards;

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
        SELECT_REQUEST.setQuery(TABLE_NAME);

        notifiers.add(NotifierType.EMAIL);

        CHANGED_USERS.add(new Tuple4<>(USER_ID, USER_ID, 0, (long) 0));
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
        SELECT_REQUEST.setMessage("");
        List<SelectRequest> requests = new ArrayList<>(Collections.singletonList(SELECT_REQUEST));

        when(leaderboardService.getLeaderboardChanges(OLD_LEADERBOARD, NEW_LEADERBOARD)).thenReturn(CHANGED_USERS);
        when(selectRequestService.getSpecificRequests(contest.getQueryIds(), Collections.singletonList(SELECT_REQUEST))).thenReturn(Collections.singleton(SELECT_REQUEST));
        when(selectRequestService.getRequestsForTable(TABLE_NAME)).thenReturn(requests);
        when(flinkTableService.executeLeaderboardQuery(eq(SELECT_REQUEST), any())).thenReturn(Collections.singleton(USER_ID));
        when(userDetailsService.getUserDetailsById(NEW_LEADERBOARD.getUserIdByPosition(0))).thenReturn(FIRST_USER_DETAILS);
        when(userDetailsService.getUserDetailsById(NEW_LEADERBOARD.getUserIdByPosition(1))).thenReturn(SECOND_USER_DETAILS);
        when(contest.getNotifiers()).thenReturn(Collections.singleton(NotifierType.EMAIL));
        when(userDetailsService.getUserDetailsById(any())).thenReturn(FIRST_USER_DETAILS);

        //Act
        leaderboardNotifierService.lookForLeaderboardChanges(contest, NEW_LEADERBOARD);

        //Assert
        verify(leaderboardService, times(1)).getLeaderboardChanges(OLD_LEADERBOARD, NEW_LEADERBOARD);
        verify(selectRequestService, times(1)).getSpecificRequests(contest.getQueryIds(), Collections.singletonList(SELECT_REQUEST));
        verify(selectRequestService, times(1)).getRequestsForTable(TABLE_NAME);
        verify(flinkTableService, times(1)).executeLeaderboardQuery(eq(SELECT_REQUEST), any());
        verify(userDetailsService, times(1)).getUserDetailsById(any());
        verify(contest, times(2)).getContestId();
        verify(contest, times(1)).getNotifiers();
        verify(notificationService, times(1)).notify(any(), any(), any());
        verify(userDetailsService, times(1)).getUserDetailsById(any());
    }

    @Test
    public void notifyAllChangesTest() throws Exception {
        //Arrange
        SELECT_REQUEST.setReceivers("1.,Common");
        SELECT_REQUEST.setMessage("");
        List<SelectRequest> requests = new ArrayList<>(Collections.singletonList(SELECT_REQUEST));

        when(leaderboardService.getLeaderboardChanges(OLD_LEADERBOARD, NEW_LEADERBOARD)).thenReturn(CHANGED_USERS);
        when(selectRequestService.getSpecificRequests(contest.getQueryIds(), Collections.singletonList(SELECT_REQUEST))).thenReturn(Collections.singleton(SELECT_REQUEST));
        when(selectRequestService.getRequestsForTable(TABLE_NAME)).thenReturn(requests);
        when(flinkTableService.executeLeaderboardQuery(eq(SELECT_REQUEST), any())).thenReturn(Collections.singleton(USER_ID));
        when(userDetailsService.turnUsersToUserIds(SELECT_REQUEST.getReceivers())).thenReturn(Collections.singleton("1"));
        when(contest.getNotifiers()).thenReturn(Collections.singleton(NotifierType.EMAIL));
        when(contest.getNotifiers()).thenReturn(notifiers);
        when(userDetailsService.getUserDetailsById(any())).thenReturn(FIRST_USER_DETAILS);

        //Act
        leaderboardNotifierService.lookForLeaderboardChanges(contest, NEW_LEADERBOARD);

        //Assert
        verify(leaderboardService, times(1)).getLeaderboardChanges(OLD_LEADERBOARD, NEW_LEADERBOARD);
        verify(selectRequestService, times(1)).getSpecificRequests(contest.getQueryIds(), Collections.singletonList(SELECT_REQUEST));
        verify(selectRequestService, times(1)).getRequestsForTable(TABLE_NAME);
        verify(flinkTableService, times(1)).executeLeaderboardQuery(eq(SELECT_REQUEST), any());
        verify(userDetailsService, times(1)).turnUsersToUserIds(SELECT_REQUEST.getReceivers());
        verify(contest, times(2)).getContestId();
        verify(contest, times(3)).getNotifiers();
        verify(notificationService, times(1)).notify(any(), any());
        verify(userDetailsService, times(3)).getUserDetailsById(any());
    }

    @Test
    public void notifyCommonChangesTest() throws Exception {
        //Arrange
        SELECT_REQUEST.setReceivers("Common");
        SELECT_REQUEST.setMessage("");
        List<SelectRequest> requests = new ArrayList<>(Collections.singletonList(SELECT_REQUEST));

        when(leaderboardService.getLeaderboardChanges(OLD_LEADERBOARD, NEW_LEADERBOARD)).thenReturn(CHANGED_USERS);
        when(selectRequestService.getSpecificRequests(contest.getQueryIds(), Collections.singletonList(SELECT_REQUEST))).thenReturn(Collections.singleton(SELECT_REQUEST));
        when(selectRequestService.getRequestsForTable(TABLE_NAME)).thenReturn(requests);
        when(flinkTableService.executeLeaderboardQuery(eq(SELECT_REQUEST), any())).thenReturn(Collections.singleton(USER_ID));
        when(contest.getNotifiers()).thenReturn(notifiers);
        when(userDetailsService.getUserDetailsById(any())).thenReturn(FIRST_USER_DETAILS);

        //Act
        leaderboardNotifierService.lookForLeaderboardChanges(contest, NEW_LEADERBOARD);

        //Assert
        verify(leaderboardService, times(1)).getLeaderboardChanges(OLD_LEADERBOARD, NEW_LEADERBOARD);
        verify(selectRequestService, times(1)).getSpecificRequests(contest.getQueryIds(), Collections.singletonList(SELECT_REQUEST));
        verify(selectRequestService, times(1)).getRequestsForTable(TABLE_NAME);
        verify(flinkTableService, times(1)).executeLeaderboardQuery(eq(SELECT_REQUEST), any());
        verify(contest, times(2)).getContestId();
        verify(contest, times(2)).getNotifiers();
        verify(notificationService, times(1)).notify(any(), any());
        verify(userDetailsService, times(2)).getUserDetailsById(any());
    }
}
