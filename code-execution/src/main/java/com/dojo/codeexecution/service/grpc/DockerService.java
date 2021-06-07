package com.dojo.codeexecution.service.grpc;

import com.dojo.codeexecution.DockerEventRequest;
import com.dojo.codeexecution.DockerEventResponse;
import com.dojo.codeexecution.DockerServiceGrpc;
import com.dojo.codeexecution.ImageRequest;
import com.dojo.codeexecution.ImageResponse;
import com.dojo.codeexecution.StopRequest;
import com.dojo.codeexecution.StopResponse;
import com.dojo.codeexecution.service.grpc.handler.DockerEventUpdateHandler;
import com.dojo.codeexecution.service.grpc.handler.ImageUpdateHandler;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DockerService extends DockerServiceGrpc.DockerServiceImplBase {
    private final ImageUpdateHandler imageUpdateHandler;
    private final DockerEventUpdateHandler dockerEventUpdateHandler;

    @Autowired
    public DockerService(ImageUpdateHandler imageUpdateHandler, DockerEventUpdateHandler dockerEventUpdateHandler) {
        this.imageUpdateHandler = imageUpdateHandler;
        this.dockerEventUpdateHandler = dockerEventUpdateHandler;
    }

    @Override
    public void getImageResults(ImageRequest request, StreamObserver<ImageResponse> responseObserver) {
        imageUpdateHandler.addObserver(request.getId(), responseObserver);
    }

    @Override
    public void getDockerEvents(DockerEventRequest request, StreamObserver<DockerEventResponse> responseObserver) {
        dockerEventUpdateHandler.addObserver(request.getId(), responseObserver);
    }

    @Override
    public void stopNotifications(StopRequest request, StreamObserver<StopResponse> responseObserver) {
        String requestId = request.getId();
        imageUpdateHandler.removeObserver(requestId).onCompleted();
        dockerEventUpdateHandler.removeObserver(requestId).onCompleted();

        responseObserver.onNext(StopResponse.newBuilder().build());
        responseObserver.onCompleted();
    }
}
