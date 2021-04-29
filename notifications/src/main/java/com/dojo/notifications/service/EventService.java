package com.dojo.notifications.service;

import com.dojo.notifications.grpc.EventClient;
import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.model.contest.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class EventService {

    private final EventClient eventClient;

    private Map<String, Event> eventRepo;
    private Map<String, String> gameServerRepo;
    private final Map<String, Contest> contestRepo = new HashMap<>();

    @Autowired
    public EventService(EventClient eventClient) {
        this.eventClient = eventClient;
    }

    public Collection<Event> getAllEvents() {
        if (eventRepo == null) {

            Map<Event, String> eventMap = eventClient.getAllEvents();

            eventRepo = new HashMap<>();
            gameServerRepo = new HashMap<>();

            eventMap.forEach((event, url) -> {
                eventRepo.put(event.getRoomName(), event);
                gameServerRepo.put(event.getRoomName(), url);
            });

        }
        return Collections.unmodifiableCollection(eventRepo.values());
    }

    public void invalidateEventsCache() {
        eventRepo = null;
    }

    public Event getEventByRoomName(String roomName) {
        if (eventRepo == null || !eventRepo.containsKey(roomName)) {
            return null;
        }
        return eventRepo.get(roomName);
    }

    public Collection<Contest> getAllContests() {
        return contestRepo.values();
    }

    public Contest getContestById(String contestId) {
        return contestRepo.get(contestId);
    }

    public void addContest(Contest contest) {
        contestRepo.put(contest.getContestId(), contest);
    }

    public void removeContest(String contestId) {
        contestRepo.remove(contestId);
    }

    public String getGameServerForContest(String contestId) {
        return gameServerRepo.get(contestId);
    }
}
