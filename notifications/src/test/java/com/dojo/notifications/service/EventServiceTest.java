package com.dojo.notifications.service;

import com.dojo.notifications.grpc.EventClient;
import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.model.contest.Event;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(SpringJUnit4ClassRunner.class)
public class EventServiceTest {

    private static final String ROOM = "room";
    private static final String GAME = "game";
    private static final String GAME_SERVER_URL = "localhost:9090";

    @Mock
    private EventClient eventClient;

    private EventService eventService;

    private Event testEvent;

    private Contest contest;

    @Before
    public void init() {
        eventService = new EventService(eventClient);

        testEvent = new Event(ROOM, GAME);
        Map<Event, String> events = new HashMap<>();
        events.put(testEvent, GAME_SERVER_URL);

        when(eventClient.getAllEvents()).thenReturn(events);
        eventService.getAllEvents();

        contest = new Contest();
        contest.setContestId(ROOM);
        contest.setTitle(GAME);
    }

    @Test
    public void getAllEventsTest() {
        Collection<Event> expected = Collections.singletonList(testEvent);
        Collection<Event> actual = eventService.getAllEvents();

        verify(eventClient, times(1)).getAllEvents();
        assertEquals(expected.size(), actual.size());
    }

    @Test
    public void getEventByRoomNameTest() {
        Event expected = testEvent;

        Event actual = eventService.getEventByRoomName(ROOM);

        assertEquals(expected, actual);
    }

    @Test
    public void invalidateEventsCacheTest() {
        Event event = eventService.getEventByRoomName(ROOM);
        assertNotNull(event);

        eventService.invalidateEventsCache();
        event = eventService.getEventByRoomName(ROOM);
        assertNull(event);
    }

    @Test
    public void getAllContestsEmptyTest() {
        assertTrue(eventService.getAllContests().isEmpty());
    }

    @Test
    public void getAllContestsNotEmptyTest() {
        eventService.addContest(contest);

        assertEquals(eventService.getAllContests().size(), 1);
    }

    @Test
    public void getContestByIdTest() {
        eventService.addContest(contest);

        Contest actual = eventService.getContestById(ROOM);

        assertEquals(contest, actual);
    }

    @Test
    public void addContestTest() {
        assertTrue(eventService.getAllContests().isEmpty());

        eventService.addContest(contest);

        assertEquals(1, eventService.getAllContests().size());
    }

    @Test
    public void removeContestTest() {
        eventService.addContest(contest);
        assertFalse(eventService.getAllContests().isEmpty());

        eventService.removeContest(ROOM);

        assertTrue(eventService.getAllContests().isEmpty());
    }

    @Test
    public void getGameServerForContestTest() {
        String actual = eventService.getGameServerForContest(ROOM);
        assertEquals(GAME_SERVER_URL, actual);
    }
}
