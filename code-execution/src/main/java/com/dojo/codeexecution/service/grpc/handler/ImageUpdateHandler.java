package com.dojo.codeexecution.service.grpc.handler;

import com.dojo.codeexecution.ImageResponse;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ImageUpdateHandler {
    private final Map<String, StreamObserver<ImageResponse>> streamObservers;

    public ImageUpdateHandler() {
        this.streamObservers = new HashMap<>();
    }

    public void addObserver(String id, StreamObserver<ImageResponse> observer) {
        this.streamObservers.put(id, observer);
    }

    public StreamObserver<ImageResponse> removeObserver(String id) {
        return this.streamObservers.remove(id);
    }

    public void sendUpdate(String tag, String message) {
        ImageResponse response = ImageResponse.newBuilder()
                .setTag(tag)
                .setMessage(message)
                .build();

        this.streamObservers.forEach((id, observer) -> observer.onNext(response));
    }
}
