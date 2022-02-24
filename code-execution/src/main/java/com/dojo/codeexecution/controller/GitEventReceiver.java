package com.dojo.codeexecution.controller;

import com.dojo.codeexecution.service.docker.DockerService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class GitEventReceiver {
    public static final String PAYLOAD_FIELD = "repository";
    public static final String IMAGE_TAG_KEY = "name";
    public static final String REPO_KEY = "full_name";
    private static final String REPO_PREFIX = "gamified-hiring-";

    @Autowired
    private DockerService dockerService;

    @GetMapping(path = "/build")
    public String buildParent() {
        dockerService.buildImage();
        return "OK";
    }

    //Currently not able to trigger the webhook which calls this endpoint from github
    @GetMapping(path = "/run")
    public String runContainer(@RequestBody Map<String, Object> payload) {
        Object imageTag = new JSONObject(payload).getJSONObject(PAYLOAD_FIELD)
                .get(IMAGE_TAG_KEY);
        Object username = new JSONObject(payload).getJSONObject(PAYLOAD_FIELD)
                .get(REPO_KEY);
        dockerService.runContainer((String) imageTag, ((String) username).split("/")[1].replace(REPO_PREFIX, ""));
        return "OK";
    }
}

