package com.dojo.codeexecution.util;

import com.dojo.codeexecution.service.DockerService;
import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.Callable;

public class GithubPushSolutionTask implements Callable<String> {

    public static final String PAYLOAD_FIELD = "repository";
    public static final String IMAGE_TAG_KEY = "name";
    public static final String REPO_KEY = "full_name";
    private static final String REPO_PREFIX = "gamified-hiring-";

    private final DockerService dockerService;
    private final Map<String, Object> payload;

    public GithubPushSolutionTask(DockerService dockerService, Map<String, Object> payload) {
        this.dockerService = dockerService;
        this.payload = payload;
    }

    @Override
    public String call() {
        Object imageTag = new JSONObject(payload).getJSONObject(PAYLOAD_FIELD)
                .get(IMAGE_TAG_KEY);
        Object username = new JSONObject(payload).getJSONObject(PAYLOAD_FIELD)
                .get(REPO_KEY);
        dockerService.runContainer((String) imageTag, ((String) username).split("/")[1].replace(REPO_PREFIX, ""));
        return "OK";
    }
}
