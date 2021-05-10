package com.dojo.notifications.grpc;

import com.codenjoy.dojo.EventServiceGrpc;
import com.codenjoy.dojo.EventsRequest;
import com.codenjoy.dojo.EventsResponse;
import com.dojo.notifications.model.contest.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class EventClient {

    private final EventServiceGrpc.EventServiceBlockingStub eventServiceBlockingStub;

    @Autowired
    public EventClient(EventServiceGrpc.EventServiceBlockingStub eventServiceBlockingStub) {
        this.eventServiceBlockingStub = eventServiceBlockingStub;
    }

    public Map<Event, String> getAllEvents() {
        EventsRequest request = EventsRequest.newBuilder().build();
        EventsResponse response = eventServiceBlockingStub.getAllEvents(request);
        Map<Event, String> events = new HashMap<>();
        response.getEventList().forEach(eventResponse ->
                events.put(new Event(eventResponse.getRoomName(), eventResponse.getGameName()), eventResponse.getGameServerUrl())
        );
        return events;
    }
}
