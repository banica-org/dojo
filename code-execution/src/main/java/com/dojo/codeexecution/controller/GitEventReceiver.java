package com.dojo.codeexecution.controller;

import com.dojo.codeexecution.service.DockerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class GitEventReceiver {

    @Autowired
    DockerService dockerService;

    @PostMapping(path = "/pushEvent")
    public boolean acceptNewTaskSubmition(@RequestBody Map<String, Object> payload) {

        System.out.println("received push event" + payload);

//        say to docker to start docker image

        //reponame
        //getcode

//        Request request = new Request(task);
//        request.execute();


        return true;
    }

    @GetMapping(path = "/build")
    public String buildParent() {
        dockerService.buildImage();
        return "OK";
    }

    //Currently not able to trigger the webhook which calls this endpoint from github
    @GetMapping(path = "/run")
    public String runContainer(@RequestBody Map<String, String> payload) {
        dockerService.runContainer(payload.get("full_name"));
        return "OK";
    }
}
