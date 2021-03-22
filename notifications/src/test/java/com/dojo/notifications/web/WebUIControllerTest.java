package com.dojo.notifications.web;

import com.dojo.notifications.api.ContestController;
import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.model.contest.Game;
import com.dojo.notifications.service.GamesService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.ui.Model;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class WebUIControllerTest {

    private static final String CONTEST_ID = "id";
    private static final String CONTEST_TITLE = "title";

    private Contest contest;

    @Mock
    private Model model;

    @Mock
    private GamesService gamesService;
    @Mock
    private ContestController contestController;

    @InjectMocks
    private WebUIController webUIController;

    @Before
    public void init() {
        contest = new Contest();
        contest.setContestId(CONTEST_ID);

        when(gamesService.getAllGames()).thenReturn(Collections.emptyList());
        when(gamesService.getAllContests()).thenReturn(Collections.emptyList());
    }

    @Test
    public void newContestTest() {
        Game game = mock(Game.class);
        when(game.getTitle()).thenReturn(CONTEST_TITLE);
        when(gamesService.getGameById(CONTEST_ID)).thenReturn(game);

        webUIController.newContest(contest, model);

        verify(gamesService, times(1)).getGameById(CONTEST_ID);
        verify(contestController, times(1)).subscribeForContest(contest);
    }

    @Test
    public void editContestTest() {
        when(gamesService.getContestById(CONTEST_ID)).thenReturn(contest);

        webUIController.editContest(CONTEST_ID, model);

        verify(gamesService, times(1)).getContestById(CONTEST_ID);
        verify(model, times(1)).addAttribute(anyString(), eq(contest));
        verify(model, times(2)).addAttribute(anyString(), eq(Collections.emptyList()));
    }

    @Test
    public void stopContestTest() {
        webUIController.stopContest(CONTEST_ID, model);

        verify(contestController, times(1)).stopNotifications(CONTEST_ID);
    }

    @Test
    public void gamesRefreshTest() {
        webUIController.gamesRefresh(model);

        verify(gamesService, times(1)).invalidateGamesCache();
    }
}
