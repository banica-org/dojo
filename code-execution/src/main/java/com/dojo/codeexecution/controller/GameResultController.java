package com.dojo.codeexecution.controller;

import com.dojo.codeexecution.model.TestResult;
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
    @Autowired
    private RestTemplate restTemplate;

    @PostMapping(path = "/test/result")
    public void testResult(@RequestBody TestResult testResult) {
        String username = testResult.getUsername();
        int points = testResult.getPoints();
        final String url = "http://localhost:8080/codenjoy-contest/rest/game/update/" + username + "/score";
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Double> entity = new HttpEntity(points, headers);
        restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    }
}
