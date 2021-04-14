package com.dojo.notifications.grpc.docker;

import com.dojo.codeexecution.DockerServiceGrpc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DockerClient {
    private final DockerServiceGrpc.DockerServiceStub dockerServiceStub;

    @Autowired
    public DockerClient(DockerServiceGrpc.DockerServiceStub dockerServiceStub) {
        this.dockerServiceStub = dockerServiceStub;
    }
}
