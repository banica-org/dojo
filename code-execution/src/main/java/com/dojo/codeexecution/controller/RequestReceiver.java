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
    public String getRepository(@RequestParam(value = "username") String username) throws IOException {
        if (gitManager.hasUserExistingRepository(username)) {
            return gitManager.getExistingGitHubRepository(username).toString();
        } else {
            return gitManager.createGitHubRepository(username).toString();
        }
    }
}
