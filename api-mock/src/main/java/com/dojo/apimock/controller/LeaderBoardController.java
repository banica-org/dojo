package com.dojo.apimock.controller;

import com.dojo.apimock.LeaderBoardProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class LeaderBoardController {

    private final Map<String, Integer> requestCounter;
    private final LeaderBoardProvider leaderBoardProvider;

    @Autowired
    public LeaderBoardController(LeaderBoardProvider leaderBoardProvider) {
        this.leaderBoardProvider = leaderBoardProvider;
        this.requestCounter = new ConcurrentHashMap<>();
    }

    @GetMapping("/api/v1/codenjoy/leaderboard")
    public List<Object> getLeaderBoard(@RequestParam String eventId) {
        int requestNumber = requestCounter.getOrDefault(eventId, 0);
        requestCounter.put(eventId, requestNumber + 1);
        return leaderBoardProvider.generateLeaderBoard(requestNumber, eventId);
    }

    @GetMapping("/api/v1/events")
    public Object getGames() {
        return leaderBoardProvider.getGames();
    }

    @GetMapping("/api/v1/users/{id}")
    public Object getUserDetails(@PathVariable String id) {
        return leaderBoardProvider.getUserDetails(id);
    }

}
