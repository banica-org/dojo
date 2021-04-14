package com.dojo.codeexecution.service.grpc.handler;

import com.dojo.codeexecution.ContainerResponse;
import com.dojo.codeexecution.model.FailedTestCase;
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

    public void sendUpdate(String status, String username, List<String> errors, List<FailedTestCase> failedTestCases) {
        this.streamObservers.forEach(observer -> observer.onNext(generateResponse(status, username, errors, failedTestCases)));
    }

    private ContainerResponse generateResponse(String status, String username, List<String> errors, List<FailedTestCase> failedTestCases) {
        ContainerResponse.Builder responseBuilder = ContainerResponse.newBuilder();
        responseBuilder.setStatus(status)
                .setUsername(username);

        errors.forEach(responseBuilder::addError);
        failedTestCases.forEach(failedTestCase -> responseBuilder.addFailedTestCase(convertToFailedTestCase(failedTestCase)));
        return responseBuilder.build();
    }

    private com.dojo.codeexecution.FailedTestCase convertToFailedTestCase(FailedTestCase failedTestCase) {
        return com.dojo.codeexecution.FailedTestCase.newBuilder()
                .setMethodName(failedTestCase.getMethodName())
                .setExpected(failedTestCase.getExpected())
                .build();
    }

}
