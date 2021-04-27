package com.dojo.notifications.service;

import com.dojo.notifications.model.leaderboard.Leaderboard;
import com.dojo.notifications.model.request.SelectRequest;
import com.dojo.notifications.model.user.Participant;
import com.dojo.notifications.model.user.UserInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class FlinkTableServiceTest {

    private static final String QUERY = "SELECT * FROM Leaderboard";
    private static final String NO_NOTIFICATIONS = "NO_NOTIFICATIONS";
    private static final String POSITION_CHANGES = "POSITION_CHANGES";
    private static final String SCORE_CHANGES = "SCORE_CHANGES";


    private final Participant FIRST_PARTICIPANT = new Participant(new UserInfo("1", "FirstUser"), 100);
    private final Participant SECOND_PARTICIPANT = new Participant(new UserInfo("2", "SecondUser"), 120);
    private final Participant THIRD_PARTICIPANT = new Participant(new UserInfo("3", "ThirdUser"), 400);

    private final Leaderboard NEW_LEADERBOARD = new Leaderboard(new TreeSet<>());
    private final Leaderboard OLD_LEADERBOARD = new Leaderboard(new TreeSet<>());

    @Mock
    private SelectRequest selectRequest;

    private FlinkTableService flinkTableService;

    @Before
    public void init() {
        flinkTableService = new FlinkTableService();
    }

    @Test
    public void executeSingleQueryEmptyTest() throws Exception {
        //Arrange
        NEW_LEADERBOARD.getParticipants().addAll(Arrays.asList(FIRST_PARTICIPANT, SECOND_PARTICIPANT));
        Set<Participant> expected = Collections.emptySet();
        when(selectRequest.getQuery()).thenReturn(QUERY);
        when(selectRequest.getEventType()).thenReturn(NO_NOTIFICATIONS);
        //Act
        Set<Participant> actual = flinkTableService.executeSingleQuery(selectRequest, NEW_LEADERBOARD, null);

        //Assert
        Assert.assertEquals(expected, actual);

        verify(selectRequest, times(1)).getQuery();
        verify(selectRequest, times(2)).getEventType();
    }

    @Test
    public void executeSingleQueryEmptyExceptionTest() throws Exception {
        //Arrange
        NEW_LEADERBOARD.getParticipants().addAll(Arrays.asList(FIRST_PARTICIPANT, SECOND_PARTICIPANT));
        Set<Participant> expected = Collections.emptySet();

        //Act
        Set<Participant> actual = flinkTableService.executeSingleQuery(selectRequest, NEW_LEADERBOARD, null);

        //Assert
        Assert.assertEquals(expected, actual);

    }

    @Test
    public void positionClimbConditionTest() throws Exception {
        //Arrange
        NEW_LEADERBOARD.getParticipants().addAll(Arrays.asList(FIRST_PARTICIPANT, SECOND_PARTICIPANT, THIRD_PARTICIPANT));
        OLD_LEADERBOARD.getParticipants().addAll(Arrays.asList(FIRST_PARTICIPANT, THIRD_PARTICIPANT));

        Set<Participant> expected = Collections.singleton(SECOND_PARTICIPANT);
        when(selectRequest.getQuery()).thenReturn(QUERY);
        when(selectRequest.getEventType()).thenReturn(POSITION_CHANGES);
        when(selectRequest.getCondition()).thenReturn((long) 1);

        //Act
        Set<Participant> actual = flinkTableService.executeSingleQuery(selectRequest, NEW_LEADERBOARD, OLD_LEADERBOARD);

        //Assert
        Assert.assertEquals(expected, actual);

        verify(selectRequest, times(1)).getQuery();
        verify(selectRequest, times(1)).getEventType();
        verify(selectRequest, times(8)).getCondition();
    }

    @Test
    public void positionDownConditionTest() throws Exception {
        //Arrange
        NEW_LEADERBOARD.getParticipants().addAll(Arrays.asList(FIRST_PARTICIPANT, SECOND_PARTICIPANT, THIRD_PARTICIPANT));
        OLD_LEADERBOARD.getParticipants().addAll(Arrays.asList(FIRST_PARTICIPANT, THIRD_PARTICIPANT));

        Set<Participant> expected = Collections.singleton(FIRST_PARTICIPANT);
        when(selectRequest.getQuery()).thenReturn(QUERY);
        when(selectRequest.getEventType()).thenReturn(POSITION_CHANGES);
        when(selectRequest.getCondition()).thenReturn((long) -1);

        //Act
        Set<Participant> actual = flinkTableService.executeSingleQuery(selectRequest, NEW_LEADERBOARD, OLD_LEADERBOARD);

        //Assert
        Assert.assertEquals(expected, actual);

        verify(selectRequest, times(1)).getQuery();
        verify(selectRequest, times(1)).getEventType();
        verify(selectRequest, times(9)).getCondition();
    }

    @Test
    public void scoreUpConditionTest() throws Exception {
        //Arrange
        NEW_LEADERBOARD.getParticipants().addAll(Arrays.asList(FIRST_PARTICIPANT, SECOND_PARTICIPANT, THIRD_PARTICIPANT));
        OLD_LEADERBOARD.getParticipants().addAll(Arrays.asList(FIRST_PARTICIPANT, SECOND_PARTICIPANT));

        Set<Participant> expected = Collections.singleton(THIRD_PARTICIPANT);
        when(selectRequest.getQuery()).thenReturn(QUERY);
        when(selectRequest.getEventType()).thenReturn(SCORE_CHANGES);
        when(selectRequest.getCondition()).thenReturn((long) 400);

        //Act
        Set<Participant> actual = flinkTableService.executeSingleQuery(selectRequest, NEW_LEADERBOARD, OLD_LEADERBOARD);

        //Assert
        Assert.assertEquals(expected, actual);

        verify(selectRequest, times(1)).getQuery();
        verify(selectRequest, times(2)).getEventType();
        verify(selectRequest, times(3)).getCondition();
    }


}
