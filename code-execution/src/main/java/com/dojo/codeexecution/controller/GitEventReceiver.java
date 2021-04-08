package com.dojo.codeexecution.controller;

import com.dojo.codeexecution.service.DockerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.Collections;
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

    //develop endpoints
    @GetMapping(path = "/build/run")
    public String buildAndRunContainer() {
        File dockerfile = new File("code-execution/src/main/docker/Dockerfile");
        dockerService.runContainer(dockerService.buildImage(dockerfile, Collections.singleton("user-param"),
                "giivanov722", "docker-test-parent"));
        return "OK";
    }

    @GetMapping(path = "/run")
    public String runContainer() {
        dockerService.runContainer("user-param");
        return "OK";
    }
}
