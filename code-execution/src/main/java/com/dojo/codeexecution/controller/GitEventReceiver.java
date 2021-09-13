package com.dojo.codeexecution.controller;

import com.dojo.codeexecution.service.DockerService;
import com.dojo.codeexecution.util.GithubPushEventManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class GitEventReceiver {

    @Autowired
    GithubPushEventManager githubPushEventManager;

    @Autowired
    DockerService dockerService;

    @GetMapping(path = "/build")
    public String buildParent() {
        dockerService.buildImage();
        return "OK";
    }

    //Currently not able to trigger the webhook which calls this endpoint from github
    @GetMapping(path = "/run")
    public String runContainer(@RequestBody Map<String, Object> payload) {
       return githubPushEventManager.executeRunContainer(dockerService, payload);
    }
}

