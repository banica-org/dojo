package com.dojo.notifications.web;

import com.dojo.notifications.api.ContestController;
import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.model.contest.Event;
import com.dojo.notifications.model.request.SelectRequestModel;
import com.dojo.notifications.service.EventService;
import com.dojo.notifications.model.request.SelectRequest;
import com.dojo.notifications.service.SelectRequestService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.ui.Model;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
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
    private static final String REQUEST_NAME = "request";
    private static final String CONTEST_NAME = "contest";
    private static final String ACTION_ADD = "add";
    private static final String ACTION_START = "start";
    private static final String ACTION_DELETE = "1";

    private static final List<SelectRequest> DUMMY_SELECT_REQUEST = Collections.singletonList(new SelectRequest());

    private Contest contest;

    @Mock
    private SelectRequestModel selectRequestModel;

    @Mock
    private Model model;

    @Mock
    private SelectRequestService selectRequestService;

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
        when(selectRequestService.getAllRequests()).thenReturn(DUMMY_SELECT_REQUEST);

        webUIController.newContest(contest, model);

        verify(eventService, times(1)).getEventByRoomName(CONTEST_ID);
        verify(contestController, times(1)).subscribeForContest(contest);
        verify(selectRequestService, times(1)).getAllRequests();
    }

    @Test
    public void editContestTest() {
        when(eventService.getContestById(CONTEST_ID)).thenReturn(contest);
        when(selectRequestService.getAllRequests()).thenReturn(DUMMY_SELECT_REQUEST);

        webUIController.editContest(CONTEST_ID, model);

        verify(eventService, times(1)).getContestById(CONTEST_ID);
        verify(model, times(1)).addAttribute(anyString(), eq(contest));
        verify(model, times(2)).addAttribute(anyString(), eq(Collections.emptyList()));
        verify(selectRequestService, times(1)).getAllRequests();
    }

    @Test
    public void stopContestTest() {
        when(selectRequestService.getAllRequests()).thenReturn(DUMMY_SELECT_REQUEST);

        webUIController.stopContest(CONTEST_ID, model);

        verify(contestController, times(1)).stopNotifications(CONTEST_ID);
        verify(selectRequestService, times(1)).getAllRequests();
    }

    @Test
    public void gamesRefreshTest() {
        when(selectRequestService.getAllRequests()).thenReturn(DUMMY_SELECT_REQUEST);

        webUIController.eventsRefresh(model);

        verify(eventService, times(1)).invalidateEventsCache();
        verify(selectRequestService, times(1)).getAllRequests();
        verify(selectRequestService, times(1)).getRequests();
    }

    @Test
    public void requestsPageTest() {

        String actual = webUIController.requestsPage(model);

        Assert.assertEquals(REQUEST_NAME, actual);
    }

    @Test
    public void newRequestAddTest() {
        when(selectRequestService.getRequests()).thenReturn(DUMMY_SELECT_REQUEST);
        when(selectRequestModel.getQueryParameters()).thenReturn("");
        when(selectRequestModel.getQuerySpecification()).thenReturn("");
        when(selectRequestModel.getReceiver()).thenReturn("");
        when(selectRequestModel.getEventType()).thenReturn("");
        when(selectRequestModel.getNotificationLevel("")).thenReturn("");
        when(selectRequestModel.getDescribingMessage()).thenReturn("");
        when(selectRequestModel.getNotificationMessage()).thenReturn("");

        String actual = webUIController.newRequest(selectRequestModel, model, ACTION_ADD);

        Assert.assertEquals(CONTEST_NAME, actual);

        verify(selectRequestService, times(2)).getRequests();
        verify(selectRequestModel, times(1)).getQueryParameters();
        verify(selectRequestModel, times(1)).getQuerySpecification();
        verify(selectRequestModel, times(1)).getReceiver();
        verify(selectRequestModel, times(2)).getEventType();
        verify(selectRequestModel, times(1)).getNotificationLevel("");
        verify(selectRequestModel, times(1)).getDescribingMessage();
        verify(selectRequestModel, times(1)).getNotificationMessage();
        verify(selectRequestService, times(1)).saveRequest(any());
        verify(model, times(13)).addAttribute(any(), any());
    }

    @Test
    public void newRequestSubmitTest() {
        when(selectRequestService.getRequests()).thenReturn(DUMMY_SELECT_REQUEST);

        String actual = webUIController.newRequest(selectRequestModel, model, ACTION_ADD);

        Assert.assertEquals(CONTEST_NAME, actual);

        verify(selectRequestService, times(2)).getRequests();
    }

    @Test
    public void determineEventAddTest() {

        String actual = webUIController.determineEvent(contest, model, ACTION_ADD);

        Assert.assertEquals(REQUEST_NAME, actual);
    }

    @Test
    public void determineEventStartTest() {
        Event event = mock(Event.class);
        when(event.getGameName()).thenReturn(CONTEST_TITLE);
        when(eventService.getEventByRoomName(CONTEST_ID)).thenReturn(event);
        when(selectRequestService.getRequests()).thenReturn(DUMMY_SELECT_REQUEST);

        String actual = webUIController.determineEvent(contest, model, ACTION_START);

        Assert.assertEquals(CONTEST_NAME, actual);

        verify(eventService, times(1)).getEventByRoomName(CONTEST_ID);
        verify(contestController, times(1)).subscribeForContest(contest);
        verify(selectRequestService, times(1)).getRequests();
    }

    @Test
    public void determineEventDeleteTest() {

        String actual = webUIController.determineEvent(contest, model, ACTION_DELETE);

        Assert.assertEquals(CONTEST_NAME, actual);
    }
}
