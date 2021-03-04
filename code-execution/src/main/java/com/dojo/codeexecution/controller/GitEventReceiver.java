package com.dojo.codeexecution.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class GitEventReceiver {

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
}
