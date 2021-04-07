package com.dojo.notifications.service;

import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.model.contest.Event;
import com.dojo.notifications.service.grpc.EventClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EventService {

    private final EventClient eventClient;
    private final NotificationManagingService notificationManagingService;

    private Map<String, Event> eventRepo;
    private final Map<String, Contest> contestRepo = new HashMap<>();

    @Autowired
    public EventService(EventClient eventClient, final NotificationManagingService notificationManagingService) {
        this.eventClient = eventClient;
        this.notificationManagingService = notificationManagingService;
    }

    public Collection<Event> getAllEvents() {
        if (eventRepo == null) {

            List<Event> eventList = eventClient.getAllEvents();

            eventRepo = new HashMap<>();
            eventList.forEach(event -> eventRepo.put(event.getRoomName(), event));
        }
        return eventRepo.values();
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
        notificationManagingService.startNotifications(contest);
        contestRepo.put(contest.getContestId(), contest);
    }

    public void stopContestById(String contestId) {
        notificationManagingService.stopNotifications(contestId);
        contestRepo.remove(contestId);
    }
}
