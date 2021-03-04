package com.dojo.codeexecution.execution;

import com.dojo.codeexecution.input.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RequestExecutionQueueHolder {

    @Value("${max.parallel.executions:1}")
    private byte maxParallelExecutions;

    private AtomicInteger numberOfActiveExecutions;
    private final ConcurrentLinkedQueue<Request> executionQueue = new ConcurrentLinkedQueue<>();

    public int getNumberOfActiveExecutions() {
        return numberOfActiveExecutions.get();
    }

    public boolean hasReachedMaxExecutions() {
        return maxParallelExecutions == numberOfActiveExecutions.get();
    }

    public boolean isQueueEmpty() {
        return executionQueue.isEmpty();
    }

    public Request poll() {
        return executionQueue.poll();
    }

    public void beginExecution() {
        numberOfActiveExecutions.incrementAndGet();
    }

    public void finishExecution() {
        numberOfActiveExecutions.decrementAndGet();
    }

    public void addRequest(Request request) {
        executionQueue.add(request);
    }

    public void setMaxParallelExecutions(byte maxParallelExecutions) {
        this.maxParallelExecutions = maxParallelExecutions;
    }
}
