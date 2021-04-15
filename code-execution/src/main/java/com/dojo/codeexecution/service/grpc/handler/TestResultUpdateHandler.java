package com.dojo.codeexecution.service.grpc.handler;

import com.dojo.codeexecution.TestResultResponse;
import com.dojo.codeexecution.model.FailedTestCase;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class TestResultUpdateHandler {
    private final Set<StreamObserver<TestResultResponse>> streamObservers;

    public TestResultUpdateHandler() {
        this.streamObservers = new HashSet<>();
    }

    public void addObserver(StreamObserver<TestResultResponse> observer) {
        this.streamObservers.add(observer);
    }

    public void removeObserver(StreamObserver<TestResultResponse> observer) {
        this.streamObservers.remove(observer);
    }

    public void sendUpdate(String username, List<FailedTestCase> failedTestCases) {
        TestResultResponse.Builder responseBuilder = TestResultResponse.newBuilder()
                .setUsername(username);

        failedTestCases.forEach(failedTestCase -> responseBuilder.addFailedTestCase(convertToFailedTestCase(failedTestCase)));
        this.streamObservers.forEach(observer -> observer.onNext(responseBuilder.build()));
    }

    private com.dojo.codeexecution.FailedTestCase convertToFailedTestCase(FailedTestCase failedTestCase) {
        return com.dojo.codeexecution.FailedTestCase.newBuilder()
                .setMethodName(failedTestCase.getMethodName())
                .setExpected(failedTestCase.getExpected())
                .build();
    }
}
