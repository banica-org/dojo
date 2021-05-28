package com.dojo.notifications.service;

import com.dojo.notifications.model.docker.Container;
import com.dojo.notifications.model.docker.TestResults;
import com.dojo.notifications.model.leaderboard.Leaderboard;
import com.dojo.notifications.model.request.SelectRequest;
import com.dojo.notifications.model.user.Participant;
import com.dojo.notifications.model.user.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.apache.flink.api.java.tuple.Tuple4;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class FlinkTableServiceTest {

    private static final String LEADERBOARD_QUERY = "SELECT * FROM leaderboard";
    private static final String DOCKER_QUERY = "SELECT * FROM docker_events";
    private static final String BROKEN_QUERY = "SELECT";
    private static final String DUMMY_STRING = "DUMMY";

    private static final String ID = "1";

    private final Participant FIRST_PARTICIPANT = new Participant(new UserInfo("1", "FirstUser"), 100);
    private final Participant SECOND_PARTICIPANT = new Participant(new UserInfo("2", "SecondUser"), 120);

    private final String CONTAINER_STATUS = "exited";
    private final String CODE_EXECUTION = "success";
    private final Integer FAILED_TEST_CASES = 1;

    private final Leaderboard NEW_LEADERBOARD = new Leaderboard(new TreeSet<>());
    private final List<Tuple4<String, String, Integer, Long>> CHANGED_USERS = new ArrayList<>();

    @Value("classpath:static/flink-tables.json")
    private Resource flinkTables;

    @Mock
    private Container container;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private TestResults testResults;

    @Mock
    private SelectRequest selectRequest;

    private FlinkTableService flinkTableService;

    @Before
    public void init() throws IOException {
        flinkTableService = new FlinkTableService();
        ReflectionTestUtils.setField(flinkTableService, "tables", new ObjectMapper().readValue(flinkTables.getFile(), new TypeReference<Map<String, List<Map<String, String>>>>() {
        }));
        CHANGED_USERS.add(new Tuple4<>(DUMMY_STRING, DUMMY_STRING, 0, (long) 0));
    }

    @Test
    public void executeLeaderboardQueryEmptyTest() throws Exception {
        //Arrange
        NEW_LEADERBOARD.getParticipants().addAll(Arrays.asList(FIRST_PARTICIPANT, SECOND_PARTICIPANT));
        Set<String> expected = Collections.singleton(DUMMY_STRING);
        when(selectRequest.getQuery()).thenReturn(LEADERBOARD_QUERY);
        //Act
        Set<String> actual = flinkTableService.executeLeaderboardQuery(selectRequest, CHANGED_USERS);

        //Assert
        assertEquals(expected, actual);

        verify(selectRequest, times(1)).getQuery();
    }

    @Test(expected = Exception.class)
    public void executeLeaderboardQueryEmptyExceptionTest() throws Exception {
        //Arrange
        NEW_LEADERBOARD.getParticipants().addAll(Arrays.asList(FIRST_PARTICIPANT, SECOND_PARTICIPANT));
        Set<String> expected = Collections.emptySet();
        when(selectRequest.getQuery()).thenReturn(BROKEN_QUERY);

        //Act
        Set<String> actual = flinkTableService.executeLeaderboardQuery(selectRequest, CHANGED_USERS);

        //Assert
        assertEquals(expected, actual);

    }

    @Test
    public void executeDockerQueryContainerTest() throws Exception {
        when(selectRequest.getQuery()).thenReturn(DOCKER_QUERY);
        when(container.getUsername()).thenReturn(DUMMY_STRING);
        when(container.getStatus()).thenReturn(CONTAINER_STATUS);
        when(container.getCodeExecution()).thenReturn(CODE_EXECUTION);
        List<String> expected = Collections.singletonList(ID);

        List<String> actual = flinkTableService.executeDockerQuery(selectRequest, container, ID);

        verify(selectRequest, times(1)).getQuery();
        verify(container, times(1)).getUsername();
        verify(container, times(1)).getStatus();
        verify(container, times(1)).getCodeExecution();
        assertEquals(expected, actual);
    }

    @Test
    public void executeDockerQueryTestResultsTest() throws Exception {
        when(selectRequest.getQuery()).thenReturn(DOCKER_QUERY);
        when(testResults.getUsername()).thenReturn(DUMMY_STRING);
        when(testResults.getFailedTestCases().size()).thenReturn(FAILED_TEST_CASES);
        List<String> expected = Collections.singletonList(ID);

        List<String> actual = flinkTableService.executeDockerQuery(selectRequest, testResults, ID);

        verify(selectRequest, times(1)).getQuery();
        verify(testResults, times(1)).getUsername();
        verify(testResults, times(2)).getFailedTestCases();
        assertEquals(expected, actual);
    }
}
