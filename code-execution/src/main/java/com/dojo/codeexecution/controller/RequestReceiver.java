package com.dojo.codeexecution.controller;

import com.dojo.codeexecution.service.GitManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class RequestReceiver {

    @Autowired
    private GitManager gitManager;

    @GetMapping(value = "/repository")
    public String getRepository(@RequestParam(value = "username") String username,
                                @RequestParam(value = "game") String game) throws IOException {
        if (gitManager.hasUserExistingRepository(username, game)) {
            return gitManager.getExistingGitHubRepository(username, game).toString();
        } else {
            return gitManager.createGitHubRepository(username, game).toString();
        }
    }
}
