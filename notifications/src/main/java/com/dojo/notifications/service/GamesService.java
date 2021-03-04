package com.dojo.notifications.service;

import com.dojo.notifications.contest.Contest;
import com.dojo.notifications.contest.Game;
import com.dojo.notifications.contest.GamesList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GamesService {

    @Value("${games-api}")
    private String gamesApi;

    private final RestTemplate restTemplate;
    private final NotificationManagingService notificationManagingService;

    private Map<String, Game> gameRepo;
    private final Map<String, Contest> contestRepo = new HashMap<>();

    @Autowired
    public GamesService(final NotificationManagingService notificationManagingService) {
        this.notificationManagingService = notificationManagingService;
        this.restTemplate = new RestTemplate();
    }

    public Collection<Game> getAllGames() {
        if (gameRepo == null) {
            ResponseEntity<GamesList> responseEntity = restTemplate.exchange(gamesApi,
                    HttpMethod.GET, null, new ParameterizedTypeReference<GamesList>() {
                    });
            List<Game> games = responseEntity.getBody().getItems();
            gameRepo = new HashMap<>();
            games.forEach(game -> gameRepo.put(game.getId(), game));
        }
        return gameRepo.values();
    }

    public void invalidateGamesCache() {
        gameRepo = null;
    }

    public Game getGameById(String id) {
        return gameRepo.get(id);
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
