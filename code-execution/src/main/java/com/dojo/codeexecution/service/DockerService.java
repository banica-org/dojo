package com.dojo.codeexecution.service;


public interface DockerService {
    String buildImage();

    void runContainer(String imageTag);
}
