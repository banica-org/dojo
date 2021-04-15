package com.dojo.apimock.service;

import com.codenjoy.dojo.Event;
import com.codenjoy.dojo.EventServiceGrpc;
import com.codenjoy.dojo.EventsRequest;
import com.codenjoy.dojo.EventsResponse;
import com.dojo.apimock.LeaderBoardProvider;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class EventService extends EventServiceGrpc.EventServiceImplBase {
    private final LeaderBoardProvider leaderBoardProvider;

    @Autowired
    public EventService(LeaderBoardProvider leaderBoardProvider) {
        this.leaderBoardProvider = leaderBoardProvider;
    }

    @Override
    public void getAllEvents(EventsRequest request, StreamObserver<EventsResponse> responseObserver) {
        Object ob = leaderBoardProvider.getGames();
        responseObserver.onNext(generateEvents(ob));
        responseObserver.onCompleted();
    }

    private EventsResponse generateEvents(Object object) {
        List<Map<String, String>> games = (List<Map<String, String>>) object;

        EventsResponse.Builder builder = EventsResponse.newBuilder();

        games.forEach(map -> {
            Event event = Event.newBuilder()
                    .setRoomName(map.get("roomName"))
                    .setGameName(map.get("gameName"))
                    .build();
            builder.addEvent(event);
        });

        return builder.build();
    }

}
