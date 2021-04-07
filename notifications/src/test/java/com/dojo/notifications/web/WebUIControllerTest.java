package com.dojo.notifications.web;

import com.dojo.notifications.api.ContestController;
import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.model.contest.Event;
import com.dojo.notifications.service.EventService;
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
    private EventService eventService;
    @Mock
    private ContestController contestController;

    @InjectMocks
    private WebUIController webUIController;

    @Before
    public void init() {
        contest = new Contest();
        contest.setContestId(CONTEST_ID);

        when(eventService.getAllEvents()).thenReturn(Collections.emptyList());
        when(eventService.getAllContests()).thenReturn(Collections.emptyList());
    }

    @Test
    public void newContestTest() {
        Event event = mock(Event.class);
        when(event.getGameName()).thenReturn(CONTEST_TITLE);
        when(eventService.getEventByRoomName(CONTEST_ID)).thenReturn(event);

        webUIController.newContest(contest, model);

        verify(eventService, times(1)).getEventByRoomName(CONTEST_ID);
        verify(contestController, times(1)).subscribeForContest(contest);
    }

    @Test
    public void editContestTest() {
        when(eventService.getContestById(CONTEST_ID)).thenReturn(contest);

        webUIController.editContest(CONTEST_ID, model);

        verify(eventService, times(1)).getContestById(CONTEST_ID);
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
        webUIController.eventsRefresh(model);

        verify(eventService, times(1)).invalidateEventsCache();
    }
}
