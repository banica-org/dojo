package com.dojo.codeexecution.service.grpc;

import com.dojo.codeexecution.ContainerRequest;
import com.dojo.codeexecution.ContainerResponse;
import com.dojo.codeexecution.DockerServiceGrpc;
import com.dojo.codeexecution.ImageRequest;
import com.dojo.codeexecution.ImageResponse;
import com.dojo.codeexecution.service.grpc.handler.ContainerUpdateHandler;
import com.dojo.codeexecution.service.grpc.handler.ImageUpdateHandler;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DockerService extends DockerServiceGrpc.DockerServiceImplBase {
    private final ImageUpdateHandler imageUpdateHandler;
    private final ContainerUpdateHandler containerUpdateHandler;

    @Autowired
    public DockerService(ImageUpdateHandler imageUpdateHandler, ContainerUpdateHandler containerUpdateHandler) {
        this.imageUpdateHandler = imageUpdateHandler;
        this.containerUpdateHandler = containerUpdateHandler;
    }

    @Override
    public void getImageResults(ImageRequest request, StreamObserver<ImageResponse> responseObserver) {
        imageUpdateHandler.addObserver(responseObserver);
    }

    @Override
    public void getContainerResults(ContainerRequest request, StreamObserver<ContainerResponse> responseObserver) {
        containerUpdateHandler.addObserver(responseObserver);
    }
}
