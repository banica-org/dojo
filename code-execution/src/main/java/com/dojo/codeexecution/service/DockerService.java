package com.dojo.codeexecution.service;


public interface DockerService {
    void buildImage();

    void runContainer(String imageTag, String username);
}
