package com.dojo.codeexecution.controller;

import com.dojo.codeexecution.config.CodenjoyConfigProperties;
import com.dojo.codeexecution.model.TestResult;
import com.dojo.codeexecution.service.grpc.handler.DockerEventUpdateHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class GameResultController {
    private final DockerEventUpdateHandler dockerEventUpdateHandler;

    private final RestTemplate restTemplate;
    private final CodenjoyConfigProperties codenjoyConfigProperties;

    @Autowired
    public GameResultController(DockerEventUpdateHandler dockerEventUpdateHandler, RestTemplate restTemplate, CodenjoyConfigProperties codenjoyConfigProperties) {
        this.dockerEventUpdateHandler = dockerEventUpdateHandler;
        this.restTemplate = restTemplate;
        this.codenjoyConfigProperties = codenjoyConfigProperties;
    }

    @PostMapping(path = "/test/result")
    public void testResult(@RequestBody TestResult testResult) {
        String username = testResult.getUsername();
        int points = testResult.getPoints();
        final String url = codenjoyConfigProperties.getPointsUpdateUrlStart() + username
                + codenjoyConfigProperties.getPointsUpdateUrlTail();
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Double> entity = new HttpEntity(points, headers);
        restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        dockerEventUpdateHandler.sendUpdate(username, testResult.getFailedTestCases());
    }
}
