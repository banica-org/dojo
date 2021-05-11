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
import org.apache.flink.api.java.tuple.Tuple4;

import javax.xml.bind.ValidationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class FlinkTableServiceTest {

    private static final String QUERY = "SELECT * FROM Leaderboard";
    private static final String BROKEN_QUERY = "SELECT";
    private static final String DUMMY_STRING = "DUMMY";


    private final Participant FIRST_PARTICIPANT = new Participant(new UserInfo("1", "FirstUser"), 100);
    private final Participant SECOND_PARTICIPANT = new Participant(new UserInfo("2", "SecondUser"), 120);

    private final Leaderboard NEW_LEADERBOARD = new Leaderboard(new TreeSet<>());
    private final List<Tuple4<String, String, Integer, Long>> CHANGED_USERS = new ArrayList<>();

    @Mock
    private SelectRequest selectRequest;

    private FlinkTableService flinkTableService;

    @Before
    public void init() {
        flinkTableService = new FlinkTableService();
        CHANGED_USERS.add(new Tuple4<>(DUMMY_STRING, DUMMY_STRING, 0, (long) 0));
    }

    @Test
    public void executeSingleQueryEmptyTest() throws Exception {
        //Arrange
        NEW_LEADERBOARD.getParticipants().addAll(Arrays.asList(FIRST_PARTICIPANT, SECOND_PARTICIPANT));
        Set<String> expected = Collections.singleton(DUMMY_STRING);
        when(selectRequest.getQuery()).thenReturn(QUERY);
        //Act
        Set<String> actual = flinkTableService.getNotifyIds(selectRequest, CHANGED_USERS);

        //Assert
        Assert.assertEquals(expected, actual);

        verify(selectRequest, times(1)).getQuery();
    }

    @Test(expected = Exception.class)
    public void executeSingleQueryEmptyExceptionTest() throws Exception {
        //Arrange
        NEW_LEADERBOARD.getParticipants().addAll(Arrays.asList(FIRST_PARTICIPANT, SECOND_PARTICIPANT));
        Set<String> expected = Collections.emptySet();
        when(selectRequest.getQuery()).thenReturn(BROKEN_QUERY);

        //Act
        Set<String> actual = flinkTableService.getNotifyIds(selectRequest, CHANGED_USERS);

        //Assert
        Assert.assertEquals(expected, actual);

    }
}
