package com.dojo.apimock;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

@Component
public class LeaderBoardProvider {

    @Value("classpath:static/leaderboard-responses.json")
    private Resource resourceFile;
    @Value("classpath:static/games-response.json")
    private Resource gamesResponse;
    @Value("classpath:static/users-responses.json")
    private Resource usersResponse;

    private Map<String,List<List<LinkedHashMap<String, Object>>>> scenarios;
    private Object games;
    private Map<String, Object> users;

    private static final Logger LOGGER = LoggerFactory.getLogger(LeaderBoardProvider.class);

    @PostConstruct
    private void load() throws IOException {
        this.scenarios = new ObjectMapper().readValue(resourceFile.getFile(), new TypeReference<Map<String,List<List<LinkedHashMap<String, Object>>>>>(){});
        this.games = new ObjectMapper().readValue(gamesResponse.getFile(), new TypeReference<Object>(){});
        this.users = new ObjectMapper().readValue(usersResponse.getFile(), new TypeReference<Map<String, Object>>(){});
    }

    public List<LinkedHashMap<String, Object>> generateLeaderBoard(final int requestNumber, final String eventId){
        if (!scenarios.containsKey(eventId)){
            return null;
        }
        List<List<LinkedHashMap<String, Object>>> eventLeaderboards = scenarios.get(eventId);
        int scenarioId = requestNumber < eventLeaderboards.size() ? requestNumber : eventLeaderboards.size()-1;
        List<LinkedHashMap<String, Object>> scenario =  eventLeaderboards.get(scenarioId);
        LOGGER.info("Scenario {} retrieved for event {} in request {}: {}", scenarioId, eventId, requestNumber, scenario);
        return scenario;
    }

    public Object getGames() {
        return games;
    }

    public Object getUserDetails(String id) {
        return users.get(id);
    }
}
