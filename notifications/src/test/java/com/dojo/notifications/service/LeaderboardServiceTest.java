package com.dojo.notifications.service;

import com.dojo.notifications.model.leaderboard.Leaderboard;
import com.dojo.notifications.model.user.Participant;
import com.dojo.notifications.model.user.UserInfo;
import org.apache.flink.api.java.tuple.Tuple4;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

@RunWith(SpringJUnit4ClassRunner.class)
public class LeaderboardServiceTest {

    private final String FIRST_USER_ID = "1";
    private final String SECOND_USER_ID = "2";

    private final String FIRST_USER_NAME = "FirstUser";
    private final String SECOND_USER_NAME = "SecondUser";

    private final long FIRST_USER_SCORE = 100;
    private final long SECOND_USER_SCORE = 120;

    private final long SCORE_CHANGE = 300;

    private final Participant firstParticipant = new Participant(new UserInfo(FIRST_USER_ID, FIRST_USER_NAME), FIRST_USER_SCORE);
    private final Participant secondParticipant = new Participant(new UserInfo(SECOND_USER_ID, SECOND_USER_NAME), SECOND_USER_SCORE);
    private final Participant firstUserChanged = new Participant(new UserInfo(FIRST_USER_ID, FIRST_USER_NAME), FIRST_USER_SCORE + SCORE_CHANGE);

    private final Leaderboard oldLeaderboard = new Leaderboard(new TreeSet<>());
    private final Leaderboard newLeaderboard = new Leaderboard(new TreeSet<>());

    @InjectMocks
    private LeaderboardService leaderboardService;

    @Before
    public void init() {
        oldLeaderboard.getParticipants().addAll(Arrays.asList(firstParticipant, secondParticipant));
        newLeaderboard.getParticipants().addAll(Arrays.asList(secondParticipant, firstUserChanged));
    }

    @Test
    public void getLeaderboardChangesTest() {
        Tuple4<String, String, Integer, Long> first = new Tuple4<>(FIRST_USER_ID, FIRST_USER_NAME, 1, SCORE_CHANGE);
        Tuple4<String, String, Integer, Long> second = new Tuple4<>(SECOND_USER_ID, SECOND_USER_NAME, -1, 0L);

        List<Tuple4<String, String, Integer, Long>> expected = new ArrayList<>();
        expected.add(first);
        expected.add(second);

        List<Tuple4<String, String, Integer, Long>> actual = leaderboardService.getLeaderboardChanges(oldLeaderboard, newLeaderboard);

        Assert.assertEquals(expected, actual);
    }
}
