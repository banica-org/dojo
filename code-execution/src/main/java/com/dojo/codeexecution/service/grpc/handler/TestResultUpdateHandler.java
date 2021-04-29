package com.dojo.codeexecution.service.grpc.handler;

import com.dojo.codeexecution.TestResultResponse;
import com.dojo.codeexecution.model.FailedTestCase;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TestResultUpdateHandler {
    private final Map<String, StreamObserver<TestResultResponse>> streamObservers;

    public TestResultUpdateHandler() {
        this.streamObservers = new HashMap<>();
    }

    public void addObserver(String id, StreamObserver<TestResultResponse> observer) {
        this.streamObservers.put(id, observer);
    }

    public StreamObserver<TestResultResponse> removeObserver(String id) {
        return this.streamObservers.remove(id);
    }

    public void sendUpdate(String username, List<FailedTestCase> failedTestCases) {
        TestResultResponse.Builder responseBuilder = TestResultResponse.newBuilder()
                .setUsername(username);

        failedTestCases.forEach(failedTestCase -> responseBuilder.addFailedTestCase(convertToFailedTestCase(failedTestCase)));
        this.streamObservers.forEach((id, observer) -> observer.onNext(responseBuilder.build()));
    }

    private com.dojo.codeexecution.FailedTestCase convertToFailedTestCase(FailedTestCase failedTestCase) {
        return com.dojo.codeexecution.FailedTestCase.newBuilder()
                .setMethodName(failedTestCase.getMethodName())
                .setExpected(failedTestCase.getExpected())
                .build();
    }
}
