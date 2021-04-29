package com.dojo.notifications.grpc;

import com.codenjoy.dojo.EventServiceGrpc;
import com.codenjoy.dojo.EventsRequest;
import com.codenjoy.dojo.EventsResponse;
import com.dojo.notifications.model.contest.Event;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class EventClientTest {

    @Mock
    private EventServiceGrpc.EventServiceBlockingStub eventServiceBlockingStub;

    private EventClient eventClient;

    @Before
    public void init() {
        eventClient = new EventClient(eventServiceBlockingStub);
    }

    @Test
    public void getAllEventsTest() {
        EventsRequest request = EventsRequest.newBuilder().build();
        EventsResponse response = EventsResponse.newBuilder().build();
        when(eventServiceBlockingStub.getAllEvents(request)).thenReturn(response);
        Map<Event, String> expected = new HashMap<>();

        Map<Event, String> actual = eventClient.getAllEvents();

        verify(eventServiceBlockingStub, times(1)).getAllEvents(request);
        assertEquals(expected, actual);
    }
}
