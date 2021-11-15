package com.dojo.codeexecution.controller;

import com.dojo.codeexecution.service.GitManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class RequestReceiver {

    private final static Logger LOGGER = LoggerFactory.getLogger(RequestReceiver.class);

    @Autowired
    private GitManager gitManager;

    @GetMapping(value = "/repository")
    public String getRepository(@RequestParam(value = "username") String usernameAndGame) throws IOException {
        String game = getGame(usernameAndGame);
        String githubUsername = usernameAndGame.replace("-" + game, "");
        if (gitManager.hasUserExistingRepository(githubUsername, game)) {
            return gitManager.getExistingGitHubRepository(githubUsername, game).toString();
        } else {
            return gitManager.createGitHubRepository(githubUsername, game).toString();
        }
    }

    private String getGame(String usernameAndGame) {
        String[] splitUsername = usernameAndGame.split("-");
        return splitUsername[splitUsername.length - 1];
    }
}
