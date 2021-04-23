package com.dojo.codeexecution.service.grpc;

import com.dojo.codeexecution.ContainerRequest;
import com.dojo.codeexecution.ContainerResponse;
import com.dojo.codeexecution.DockerServiceGrpc;
import com.dojo.codeexecution.ImageRequest;
import com.dojo.codeexecution.ImageResponse;
import com.dojo.codeexecution.StopRequest;
import com.dojo.codeexecution.StopResponse;
import com.dojo.codeexecution.TestResultRequest;
import com.dojo.codeexecution.TestResultResponse;
import com.dojo.codeexecution.service.grpc.handler.ContainerUpdateHandler;
import com.dojo.codeexecution.service.grpc.handler.ImageUpdateHandler;
import com.dojo.codeexecution.service.grpc.handler.TestResultUpdateHandler;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DockerService extends DockerServiceGrpc.DockerServiceImplBase {
    private final ImageUpdateHandler imageUpdateHandler;
    private final ContainerUpdateHandler containerUpdateHandler;
    private final TestResultUpdateHandler testResultUpdateHandler;

    @Autowired
    public DockerService(ImageUpdateHandler imageUpdateHandler, ContainerUpdateHandler containerUpdateHandler, TestResultUpdateHandler testResultUpdateHandler) {
        this.imageUpdateHandler = imageUpdateHandler;
        this.containerUpdateHandler = containerUpdateHandler;
        this.testResultUpdateHandler = testResultUpdateHandler;
    }

    @Override
    public void getImageResults(ImageRequest request, StreamObserver<ImageResponse> responseObserver) {
        imageUpdateHandler.addObserver(request.getId(), responseObserver);
    }

    @Override
    public void getContainerResults(ContainerRequest request, StreamObserver<ContainerResponse> responseObserver) {
        containerUpdateHandler.addObserver(request.getId(), responseObserver);
    }

    @Override
    public void getTestResults(TestResultRequest request, StreamObserver<TestResultResponse> responseObserver) {
        testResultUpdateHandler.addObserver(request.getId(), responseObserver);
    }

    @Override
    public void stopNotifications(StopRequest request, StreamObserver<StopResponse> responseObserver) {
        StreamObserver<ImageResponse> imageResponseStreamObserver = imageUpdateHandler.removeObserver(request.getId());
        StreamObserver<ContainerResponse> containerResponseStreamObserver = containerUpdateHandler.removeObserver(request.getId());
        StreamObserver<TestResultResponse> testResultResponseStreamObserver = testResultUpdateHandler.removeObserver(request.getId());

        imageResponseStreamObserver.onCompleted();
        containerResponseStreamObserver.onCompleted();
        testResultResponseStreamObserver.onCompleted();

        responseObserver.onNext(StopResponse.newBuilder().build());
        responseObserver.onCompleted();
    }
}
