package com.dojo.codeexecution.service;

import com.dojo.codeexecution.exception.NoLogsForContainerException;
import com.dojo.codeexecution.exception.NoSuchImageFoundException;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.BuildImageCmd;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.command.PruneCmd;
import com.github.dockerjava.api.command.WaitContainerCmd;
import com.github.dockerjava.api.command.WaitContainerResultCallback;
import com.github.dockerjava.api.model.BuildResponseItem;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PruneResponse;
import com.github.dockerjava.api.model.PruneType;
import com.github.dockerjava.api.model.WaitResponse;
import com.github.dockerjava.core.command.PruneCmdImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class DockerService {
    private static final String DOCKERFILE_PATH = "code-execution/src/main/docker/Dockerfile";
    private static final String USER_NAME = "user_name";
    private static final String REPO_NAME = "repo_name";
    private final DockerClient dockerClient;

    @Autowired
    public DockerService(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    public void runContainer(String imageTag){
        CreateContainerResponse createdContainer = createContainer(imageTag);
        String containerId = createdContainer.getId();
        dockerClient.startContainerCmd(containerId).exec();
        dockerClient.waitContainerCmd(containerId)
                .exec(getWaitContainerExecutionCallback(containerId));
    }

    public void runContainerWithImageBuild(){
        File dockerfile = new File(DOCKERFILE_PATH);
        String imageId = buildImage(dockerfile, "user-param",
                "giivanov722", "docker-test-parent");
        String imageTag = getImageTag(imageId);
        runContainer(imageTag);
    }

    private String buildImage(File dockerFile, String imageTagName, String repoUsername, String repoName){
        BuildImageCmd buildImage = dockerClient.buildImageCmd()
                .withDockerfile(dockerFile)
                .withRemove(true)
                .withNoCache(true)
                .withTags(Collections.singleton(imageTagName))
                .withBuildArg(USER_NAME, repoUsername)
                .withBuildArg(REPO_NAME,repoName);
        return buildImage.exec(getImageBuildResultCallBack()).awaitImageId();
    }

    private String getImageTag(String imageId){
        String imageTag = dockerClient.inspectImageCmd(imageId)
                .exec().getRepoTags().get(0);
        if (imageTag == null){
            throw new NoSuchImageFoundException("No image found with that id");
        }
        return imageTag;
    }

    private CreateContainerResponse createContainer(String imageTag){
        List<String>args = generateShellArgs();
        return dockerClient.createContainerCmd(imageTag)
                .withCmd(args)
                .withName(imageTag.split(":")[0]).exec();
    }

    private List<String> getContainerLog(String containerId){
        LogContainerCmd logContainerCmd = dockerClient.logContainerCmd(containerId)
                .withStdOut(true).withStdErr(true).withTail(3);
        List<String> logs = new ArrayList<>();
        try {
            logContainerCmd.exec(getLogCallBack(logs)).awaitCompletion();
        } catch (InterruptedException e) {
            throw new NoLogsForContainerException("Failed to retrieve logs for container with id " + containerId);
        }
        return logs;
    }

    private void deleteContainer(String containerId){
        dockerClient.removeContainerCmd(containerId)
                .withRemoveVolumes(true)
                .withForce(true).exec();
    }

    private BuildImageResultCallback getImageBuildResultCallBack(){
        return new BuildImageResultCallback(){
            @Override
            public void onNext(BuildResponseItem item) {
                super.onNext(item);
            }
        };
    }

    private ResultCallback.Adapter<Frame> getLogCallBack(List<String> logs){
        return new ResultCallback.Adapter<Frame>(){
            @Override
            public void onNext(Frame item) {
                logs.add(item.toString());
            }
        };
    }

    private ResultCallback.Adapter<WaitResponse> getWaitContainerExecutionCallback(String containerId){
        return new ResultCallback.Adapter<WaitResponse>(){
            @Override
            public void onNext(WaitResponse object) {
                List<String> logs = getContainerLog(containerId);
                deleteContainer(containerId);
                logs.forEach(System.out::println);
                super.onNext(object);
            }
        };
    }

    private List<String> generateShellArgs(){
        List<String> args = new ArrayList<>();
        args.add("giivanov722");
        //args.add(); password or access token
        args.add("giivanov722");
        args.add("docker-test-child");
        args.add("docker-test-parent");
        return args;
    }

}
