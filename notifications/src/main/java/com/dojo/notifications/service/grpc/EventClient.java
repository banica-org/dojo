package com.dojo.notifications.service.grpc;

import com.codenjoy.dojo.EventServiceGrpc;
import com.codenjoy.dojo.EventsRequest;
import com.codenjoy.dojo.EventsResponse;
import com.dojo.notifications.model.contest.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EventClient {

    private final EventServiceGrpc.EventServiceBlockingStub eventServiceBlockingStub;

    @Autowired
    public EventClient(EventServiceGrpc.EventServiceBlockingStub eventServiceBlockingStub) {
        this.eventServiceBlockingStub = eventServiceBlockingStub;
    }


    public List<Event> getAllEvents() {
        EventsRequest request = EventsRequest.newBuilder().build();

        EventsResponse response = eventServiceBlockingStub.getAllEvents(request);

        List<Event> events = new ArrayList<>();

        response.getEventList().forEach(eventResponse ->
                events.add(new Event(eventResponse.getRoomName(), eventResponse.getGameName()))
        );

        return events;
    }
}
