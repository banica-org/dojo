package com.dojo.codeexecution.service.grpc.handler;

import com.dojo.codeexecution.ImageResponse;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class ImageUpdateHandler {
    private final Set<StreamObserver<ImageResponse>> streamObservers;

    public ImageUpdateHandler() {
        this.streamObservers = new HashSet<>();
    }

    public void addObserver(StreamObserver<ImageResponse> observer) {
        this.streamObservers.add(observer);
    }

    public void removeObserver(StreamObserver<ImageResponse> observer) {
        this.streamObservers.remove(observer);
    }

    public void sendUpdate(String tag, String message) {
        ImageResponse response = ImageResponse.newBuilder()
                .setTag(tag)
                .setMessage(message)
                .build();

        this.streamObservers.forEach(observer -> observer.onNext(response));
    }
}
