package com.dojo.codeexecution.service.grpc.handler;

import com.dojo.codeexecution.ContainerResponse;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ContainerUpdateHandler {
    private final Map<String, StreamObserver<ContainerResponse>> streamObservers;

    public ContainerUpdateHandler() {
        this.streamObservers = new HashMap<>();
    }

    public void addObserver(String id, StreamObserver<ContainerResponse> observer) {
        this.streamObservers.put(id, observer);
    }

    public StreamObserver<ContainerResponse> removeObserver(String id) {
        return this.streamObservers.remove(id);
    }

    public void sendUpdate(String status, String username, List<String> errors) {
        this.streamObservers.forEach((id, observer) -> observer.onNext(generateResponse(status, username, errors)));
    }

    private ContainerResponse generateResponse(String status, String username, List<String> logs) {
        ContainerResponse.Builder responseBuilder = ContainerResponse.newBuilder()
                .setStatus(status)
                .setUsername(username);

        logs.forEach(responseBuilder::addLog);
        return responseBuilder.build();
    }
}
