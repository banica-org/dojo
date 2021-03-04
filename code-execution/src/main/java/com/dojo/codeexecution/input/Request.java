package com.dojo.codeexecution.input;

import com.dojo.codeexecution.execution.RequestExecutionQueueHolder;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * internal representation of a single incoming request
 */
public class Request {

    private String participantName;
    private String taskName;
    private String fileAddress;

    @Autowired
    private RequestExecutionQueueHolder requestExecutionQueue;

    public Request(String taskName) {
        this.taskName = taskName;
        this.fileAddress = fileAddress;
        this.participantName = participantName;
    }

    public String getParticipantName() {
        return participantName;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getFileAddress() {
        return fileAddress;
    }

    public void execute() {
        //TODO jar file
        requestExecutionQueue.addRequest(this);
    }
}
