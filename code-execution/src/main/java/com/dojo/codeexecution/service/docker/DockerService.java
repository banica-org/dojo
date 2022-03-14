package com.dojo.codeexecution.service.docker;


public interface DockerService {
    void buildImage();

    void runContainer(String imageTag, String username);
}
