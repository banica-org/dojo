package com.dojo.codeexecution.controller;

import com.dojo.codeexecution.service.GitManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RequestReceiver {

    final static Logger logger = LoggerFactory.getLogger(RequestReceiver.class);

    @Autowired
    private GitManager gitManager;

    @GetMapping(value = "/repo")
    public String getRepo(@RequestParam(value = "email") String email) {
        try {
           return gitManager.getRepo(email).toString();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return "NOT_FOUND";
        }
    }
}
