package com.dojo.codeexecution.service;

import com.dojo.codeexecution.config.GitConfigProperties;
import com.dojo.codeexecution.config.docker.DockerConfigProperties;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.BuildImageCmd;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.model.BuildResponseItem;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.WaitResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class DockerServiceImpl implements DockerService {
    private static final String USER_NAME = "user_name";
    private static final String REPO_NAME = "repo_name";

    private final DockerClient dockerClient;
    private final DockerConfigProperties dockerConfigProperties;
    private final GitConfigProperties gitConfigProperties;

    @Autowired
    public DockerServiceImpl(DockerClient dockerClient, DockerConfigProperties dockerConfigProperties, GitConfigProperties gitConfigProperties) {
        this.dockerClient = dockerClient;
        this.dockerConfigProperties = dockerConfigProperties;
        this.gitConfigProperties = gitConfigProperties;
    }

    public void runContainer(String imageTag) {
        String containerId = createContainer(imageTag).getId();
        dockerClient.startContainerCmd(containerId).exec();
        dockerClient.waitContainerCmd(containerId)
                .exec(getWaitContainerExecutionCallback(containerId));
    }

    public String buildImage() {
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

    public List<String> getContainerLog(String containerId) throws InterruptedException {
        LogContainerCmd logContainerCmd = dockerClient.logContainerCmd(containerId)
                .withStdOut(true).withStdErr(true).withTail(3);
        List<String> logs = new ArrayList<>();
        logContainerCmd.exec(getLogCallBack(logs)).awaitCompletion();
        return logs;
    }

    public void deleteContainer(String containerId) {
        dockerClient.removeContainerCmd(containerId)
                .withRemoveVolumes(true)
                .withForce(true).exec();
    }

    private String getImageTag(String imageId) {
        return Objects.requireNonNull(dockerClient.inspectImageCmd(imageId)
                .exec().getRepoTags()).get(0);
    }

    private CreateContainerResponse createContainer(String imageTag) {
        return dockerClient.createContainerCmd(imageTag)
                .withCmd(dockerConfigProperties.getShellArguments())
                .withName(imageTag.split(":")[0]).exec();
    }

    private BuildImageResultCallback getImageBuildResultCallBack() {
        return new BuildImageResultCallback() {
            @Override
            public void onNext(BuildResponseItem item) {
                super.onNext(item);
            }
        };
    }

    private ResultCallback.Adapter<Frame> getLogCallBack(List<String> logs) {
        return new ResultCallback.Adapter<Frame>() {
            @Override
            public void onNext(Frame item) {
                logs.add(item.toString());
            }
        };
    }

    private ResultCallback.Adapter<WaitResponse> getWaitContainerExecutionCallback(String containerId) {
        return new ResultCallback.Adapter<WaitResponse>() {
            @Override
            public void onNext(WaitResponse object) {
                List<String> logs = null;
                try {
                    logs = getContainerLog(containerId);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (logs == null) {
                    logs = new ArrayList<>();
                }
                deleteContainer(containerId);
                logs.forEach(System.out::println);
                super.onNext(object);
            }
        };
    }

}
