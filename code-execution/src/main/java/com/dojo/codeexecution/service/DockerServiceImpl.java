package com.dojo.codeexecution.service;

import com.dojo.codeexecution.config.docker.DockerConfigProperties;
import com.dojo.codeexecution.config.github.GitConfigProperties;
import com.dojo.codeexecution.service.grpc.handler.ContainerUpdateHandler;
import com.dojo.codeexecution.service.grpc.handler.ImageUpdateHandler;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.BuildImageCmd;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.model.BuildResponseItem;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.PruneType;
import com.github.dockerjava.api.model.WaitResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class DockerServiceImpl implements DockerService {
    private static final String USER_NAME = "user_name";
    private static final String REPO_NAME = "repo_name";
    private static final String LOG_SEPARATOR = "STDOUT: build-log-separator";

    private final ImageUpdateHandler imageUpdateHandler;
    private final ContainerUpdateHandler containerUpdateHandler;

    private final DockerClient dockerClient;
    private final DockerConfigProperties dockerConfigProperties;
    private final GitConfigProperties gitConfigProperties;
    private final AtomicInteger containerCounter;
    private final Map<String, String> containerUserCache;
    private final ExecutorService singleThreadExecutor;

    @Autowired
    public DockerServiceImpl(ImageUpdateHandler imageUpdateHandler, ContainerUpdateHandler containerUpdateHandler,
                             DockerClient dockerClient, DockerConfigProperties dockerConfigProperties,
                             GitConfigProperties gitConfigProperties,
                             @Qualifier("buildImageSingleThreadExecutor") ExecutorService singleThreadExecutor) {
        this.imageUpdateHandler = imageUpdateHandler;
        this.containerUpdateHandler = containerUpdateHandler;
        this.dockerClient = dockerClient;
        this.dockerConfigProperties = dockerConfigProperties;
        this.gitConfigProperties = gitConfigProperties;
        this.containerCounter = new AtomicInteger();
        this.containerUserCache = new ConcurrentHashMap<>();
        this.singleThreadExecutor = singleThreadExecutor;
    }

    @PostConstruct
    private void setup() {
        buildImage();
    }

    public void runContainer(String imageTag) {
        String username = "kaloyan_dutsolov6";
        String containerId = createContainer(imageTag).getId();
        dockerClient.startContainerCmd(containerId).exec();
        addContainerUsername(containerId, username);
        String status = getContainerStatus(containerId);
        containerUpdateHandler.sendUpdate(status, containerUserCache.get(containerId), new ArrayList<>());
        dockerClient.waitContainerCmd(containerId)
                .exec(getWaitContainerExecutionCallback(containerId));
    }

    public void buildImage() {
        singleThreadExecutor.submit(() -> {
            buildImageTask();
        });
    }

    private String buildImageTask() {
        deleteUnnecessaryContainers();
        deleteUnnecessaryImages();
        BuildImageCmd buildImage = dockerClient.buildImageCmd()
                .withDockerfile(new File(dockerConfigProperties.getFilepath()))
                .withRemove(true)
                .withNoCache(true)
                .withTags(Collections.singleton(dockerConfigProperties.getParentTag()))
                .withBuildArg(USER_NAME, gitConfigProperties.getUser())
                .withBuildArg(REPO_NAME, gitConfigProperties.getParentRepositoryName());
        String imageId = buildImage.exec(getImageBuildResultCallBack()).awaitImageId();
        return getImageTag(imageId);
    }

    public List<String> getContainerLog(String containerId, List<String> logs) throws InterruptedException {
        LogContainerCmd logContainerCmd =
                dockerClient.logContainerCmd(containerId)
                        .withStdOut(true)
                        .withStdErr(true);
        StringBuilder stringBuilder = new StringBuilder();
        logContainerCmd.exec(getLogCallBack(logs, stringBuilder)).awaitCompletion();
        return logs;
    }

    public void deleteContainer(String containerId) {
        dockerClient.removeContainerCmd(containerId)
                .withRemoveVolumes(true)
                .withForce(true).exec();
        decrementContainerCounter();
    }

    private void deleteUnnecessaryImages() {
        dockerClient.pruneCmd(PruneType.IMAGES)
                .withDangling(true).exec();
    }

    private void deleteUnnecessaryContainers() {
        dockerClient.pruneCmd(PruneType.CONTAINERS).exec();
    }

    private String getImageTag(String imageId) {
        return Objects.requireNonNull(dockerClient.inspectImageCmd(imageId).exec()
                .getRepoTags())
                .get(0);
    }

    private CreateContainerResponse createContainer(String imageTag) {
        incrementContainerCounter();
        String containerName = imageTag.split(":")[0] + this.getContainerCounter();
        return dockerClient.createContainerCmd(imageTag)
                .withCmd(dockerConfigProperties.getShellArguments())
                .withName(containerName).exec();
    }

    private BuildImageResultCallback getImageBuildResultCallBack() {
        return new BuildImageResultCallback() {
            @Override
            public void onNext(BuildResponseItem item) {
                String message;
                String imageTag = dockerConfigProperties.getParentTag();
                if (item.isErrorIndicated()) {
                    message = item.getErrorDetail().getMessage();
                    imageUpdateHandler.sendUpdate(imageTag, message);
                }
                if (item.isBuildSuccessIndicated()) {
                    message = "Image built successfully!";
                    imageUpdateHandler.sendUpdate(imageTag, message);
                }
                super.onNext(item);
            }
        };
    }

    private ResultCallback.Adapter<Frame> getLogCallBack(List<String> logs, StringBuilder stringBuilder) {
        return new ResultCallback.Adapter<Frame>() {
            @Override
            public void onNext(Frame item) {
                String itemValue = item.toString();
                if (itemValue.equals(LOG_SEPARATOR)) {
                    logs.add(stringBuilder.toString());
                    stringBuilder.delete(0, stringBuilder.length());
                }
                if (!itemValue.equals(LOG_SEPARATOR)) {
                    stringBuilder.append(item.toString());
                    stringBuilder.append('\n');
                }
            }
        };
    }

    private ResultCallback.Adapter<WaitResponse> getWaitContainerExecutionCallback(String containerId) {
        return new ResultCallback.Adapter<WaitResponse>() {
            @Override
            public void onNext(WaitResponse object) {
                List<String> logs = new ArrayList<>();
                String status = getContainerStatus(containerId);
                try {
                    containerUpdateHandler.sendUpdate(status,
                            containerUserCache.get(containerId), getContainerLog(containerId, logs));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                logs.forEach(System.out::println);
                deleteContainerUsername(containerId);
                deleteContainer(containerId);
                super.onNext(object);
            }
        };
    }

    private String getContainerStatus(String containerId) {
        InspectContainerResponse inspectContainerResponse =
                dockerClient.inspectContainerCmd(containerId).exec();
        return inspectContainerResponse.getState().getStatus();
    }

    public int getContainerCounter() {
        return containerCounter.get();
    }

    private int incrementContainerCounter() {
        return containerCounter.incrementAndGet();
    }

    private int decrementContainerCounter() {
        return containerCounter.decrementAndGet();
    }

    private void addContainerUsername(String containerId, String username) {
        containerUserCache.putIfAbsent(containerId, username);
    }

    private String getContainerUsername(String containerId) {
        return containerUserCache.get(containerId);
    }

    private void deleteContainerUsername(String containerId) {
        containerUserCache.remove(containerId);
    }
}
