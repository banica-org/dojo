package com.dojo.notifications.service;

import com.dojo.notifications.contest.Contest;
import com.dojo.notifications.contest.Game;
import com.dojo.notifications.contest.GamesList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(SpringJUnit4ClassRunner.class)
public class GamesServiceTest {

    private static final String ID = "game";
    private static final String TITLE = "game";

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private NotificationManagingService notificationManagingService;

    private GamesService gamesService;

    private GamesList gamesList;
    private Game testGame;

    private Contest contest;

    @Before
    public void init() {
        gamesService = new GamesService(notificationManagingService);

        testGame = new Game();
        testGame.setId(ID);
        testGame.setTitle(TITLE);

        gamesList = new GamesList();
        gamesList.setItems(Collections.singletonList(testGame));

        addGamesInDb();

        contest = new Contest();
        contest.setContestId(ID);
        contest.setTitle(TITLE);
    }

    @Test
    public void getAllGamesNullTest() {
        Collection<Game> actual = gamesService.getAllGames();
        Collection<Game> expected = gamesList.getItems();

        verify(restTemplate, times(1))
                .exchange(anyString(), any(), any(), eq(new ParameterizedTypeReference<GamesList>() {
                }));
        assertEquals(expected.size(), actual.size());
    }

    @Test
    public void getAllGamesNonNullTest() {
        gamesService.getAllGames();
        gamesService.getAllGames();

        verify(restTemplate, times(1))
                .exchange(anyString(), any(), any(), eq(new ParameterizedTypeReference<GamesList>() {
                }));
    }

    @Test
    public void getGameByIdTest() {
        gamesService.getAllGames();
        Game expected = testGame;

        Game actual = gamesService.getGameById(ID);

        assertEquals(expected, actual);
    }

    @Test(expected = NullPointerException.class)
    public void invalidateGamesCacheTest() {
        gamesService.getAllGames();
        Game game = gamesService.getGameById(ID);
        assertNotNull(game);

        gamesService.invalidateGamesCache();
        gamesService.getGameById(ID);
    }

    @Test
    public void getAllContestsEmptyTest() {
        assertTrue(gamesService.getAllContests().isEmpty());
    }

    @Test
    public void getAllContestsNotEmptyTest() {
        gamesService.addContest(contest);

        assertEquals(gamesService.getAllContests().size(), 1);
    }

    @Test
    public void addContestTest() {
        gamesService.addContest(contest);

        verify(notificationManagingService, times(1)).startNotifications(contest);
    }

    @Test
    public void getContestByIdTest() {
        gamesService.addContest(contest);

        Contest actual = gamesService.getContestById(ID);

        assertEquals(contest, actual);
    }

    @Test
    public void stopContestById() {
        gamesService.addContest(contest);
        assertFalse(gamesService.getAllContests().isEmpty());

        gamesService.stopContestById(ID);

        assertTrue(gamesService.getAllContests().isEmpty());
        verify(notificationManagingService, times(1)).stopNotifications(ID);
    }

    private void addGamesInDb() {
        when(restTemplate
                .exchange(anyString(), any(), any(), eq(new ParameterizedTypeReference<GamesList>() {
                })))
                .thenReturn(new ResponseEntity<>(gamesList, HttpStatus.ACCEPTED));

        ReflectionTestUtils.setField(gamesService, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(gamesService, "gamesApi", "uri");
    }
}
