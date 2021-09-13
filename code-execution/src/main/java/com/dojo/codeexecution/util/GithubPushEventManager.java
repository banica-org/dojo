package com.dojo.codeexecution.util;

import com.dojo.codeexecution.service.DockerService;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Component
public class GithubPushEventManager {

    private final ExecutorService executorService;

    public GithubPushEventManager() {
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public String executeRunContainer(DockerService dockerService, Map<String, Object> payload) {
        String result = "";

        Future<String> future =
                executorService.submit(new GithubPushSolutionTask(dockerService, payload));

        try {
            result = future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return result;
    }

    @PreDestroy
    private void shutDownExecutor() {
        executorService.shutdown();
        try {
            boolean terminated = executorService.awaitTermination(500, TimeUnit.MILLISECONDS);
            if (!terminated) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            e.printStackTrace();
        }
    }
}
