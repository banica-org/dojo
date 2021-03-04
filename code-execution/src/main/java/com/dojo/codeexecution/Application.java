package com.dojo.codeexecution;

import com.dojo.codeexecution.execution.RequestExecutionQueueHolder;
import com.dojo.codeexecution.execution.RequestExecutor;
import com.dojo.codeexecution.input.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication(scanBasePackages={"com.dojo.codeexecution"})
@EnableConfigurationProperties
@EnableRetry
public class Application {

    private static RequestExecutionQueueHolder executionQueueHolder;

    @Autowired
    public void setRequestExecutionQueueHolder(RequestExecutionQueueHolder requestExecutionQueueHolder){
        Application.executionQueueHolder = requestExecutionQueueHolder;
    }

    static Request currentRequest;

    final static Runnable codeExecutionRunnable =
            () -> {
                executionQueueHolder.beginExecution();
                RequestExecutor.invokeKataClient(currentRequest.getParticipantName());
                executionQueueHolder.finishExecution();
            };

    final static Logger logger = LoggerFactory.getLogger(Application.class);

    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);

        while (true) {
            if (!executionQueueHolder.isQueueEmpty()) {
                if (executionQueueHolder.hasReachedMaxExecutions()) {
                    logger.info("New request cannot be processed at the moment because max_parallel_execution capacity has been reached.");
                } else {
                    logger.info("New request processing has begun. Previous number of active executions was " + executionQueueHolder.getNumberOfActiveExecutions());
                    currentRequest = executionQueueHolder.poll();
                    new Thread(codeExecutionRunnable).start();
                }
            } else {
                //logger.info("No requests to process.");
            }
        }
    }

}
