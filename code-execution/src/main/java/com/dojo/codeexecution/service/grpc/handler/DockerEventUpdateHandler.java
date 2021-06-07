package com.dojo.codeexecution.service.grpc.handler;

import com.dojo.codeexecution.ContainerResponse;
import com.dojo.codeexecution.DockerEventResponse;
import com.dojo.codeexecution.TestResultResponse;
import com.dojo.codeexecution.model.FailedTestCase;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DockerEventUpdateHandler {
    private final Map<String, StreamObserver<DockerEventResponse>> streamObservers;

    public DockerEventUpdateHandler() {
        this.streamObservers = new HashMap<>();
    }

    public void addObserver(String id, StreamObserver<DockerEventResponse> observer) {
        this.streamObservers.put(id, observer);
    }

    public StreamObserver<DockerEventResponse> removeObserver(String id) {
        return this.streamObservers.remove(id);
    }

    public void sendUpdate(String status, String username, List<String> logs) {
        this.streamObservers.forEach((id, observer) -> observer.onNext(generateContainerResponse(status, username, logs)));
    }

    public void sendUpdate(String username, List<FailedTestCase> failedTestCases) {
        this.streamObservers.forEach((id, observer) -> observer.onNext(generateTestResultsResponse(username, failedTestCases)));
    }

    private DockerEventResponse generateContainerResponse(String status, String username, List<String> logs) {
        ContainerResponse.Builder responseBuilder = ContainerResponse.newBuilder()
                .setStatus(status)
                .setUsername(username);

        logs.forEach(responseBuilder::addLog);
        return DockerEventResponse.newBuilder().setContainer(responseBuilder.build()).build();
    }

    private DockerEventResponse generateTestResultsResponse(String username, List<FailedTestCase> failedTestCases) {
        TestResultResponse.Builder responseBuilder = TestResultResponse.newBuilder()
                .setUsername(username);

        failedTestCases.forEach(failedTestCase -> responseBuilder.addFailedTestCase(convertToFailedTestCase(failedTestCase)));
        return DockerEventResponse.newBuilder().setTestResults(responseBuilder.build()).build();
    }

    private com.dojo.codeexecution.FailedTestCase convertToFailedTestCase(FailedTestCase failedTestCase) {
        return com.dojo.codeexecution.FailedTestCase.newBuilder()
                .setMethodName(failedTestCase.getMethodName())
                .setExpected(failedTestCase.getExpected())
                .build();
    }
}
