package com.dojo.codeexecution.service.grpc.handler;

import com.dojo.codeexecution.ContainerResponse;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class ContainerUpdateHandler {
    private final Set<StreamObserver<ContainerResponse>> streamObservers;

    public ContainerUpdateHandler() {
        this.streamObservers = new HashSet<>();
    }

    public void addObserver(StreamObserver<ContainerResponse> observer) {
        this.streamObservers.add(observer);
    }

    public void removeObserver(StreamObserver<ContainerResponse> observer) {
        this.streamObservers.remove(observer);
    }

    public void sendUpdate(String status, String username, List<String> errors) {
        this.streamObservers.forEach(observer -> observer.onNext(generateResponse(status, username, errors)));
    }

    private ContainerResponse generateResponse(String status, String username, List<String> logs) {
        ContainerResponse.Builder responseBuilder = ContainerResponse.newBuilder()
                .setStatus(status)
                .setUsername(username);

        logs.forEach(responseBuilder::addLog);
        return responseBuilder.build();
    }
}
