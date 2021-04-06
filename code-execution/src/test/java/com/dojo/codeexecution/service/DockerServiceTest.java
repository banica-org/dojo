package com.dojo.codeexecution.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.BuildImageCmd;
import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.command.RemoveContainerCmd;
import com.github.dockerjava.api.command.StartContainerCmd;
import com.github.dockerjava.api.command.WaitContainerCmd;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class DockerServiceTest {

    @InjectMocks
    private DockerService dockerService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DockerClient dockerClient;

    private static String dummyImageTag;

    private static String user_name;

    private static String repo_name;

    private static String dummyRepoUsername;

    private static String dummyRepoName;

    private static String dummyId;

    @BeforeClass
    public static void setUp() {
        dummyImageTag = "dummy-tag:latest";
        user_name = "user_name";
        repo_name = "repo_name";
        dummyRepoUsername = "dummyuser";
        dummyRepoName = "dummy-reponame";
        dummyId = "1234";
    }

    //    @Ignore
    @Test
    public void runContainer() {
        //Arrange
        List<String> args = DockerService.generateShellArgs();
        CreateContainerResponse createContainerResponse = mock(CreateContainerResponse.class);
        StartContainerCmd startContainerCmdMock = mock(StartContainerCmd.class);
        WaitContainerCmd waitContainerMock = mock(WaitContainerCmd.class);
        ResultCallback.Adapter resultCallback = mock(ResultCallback.Adapter.class);

        //Act
        when(dockerClient.createContainerCmd(dummyImageTag).withCmd(args).withName(dummyImageTag.split(":")[0])
                .exec()).thenReturn(createContainerResponse);
        when(createContainerResponse.getId()).thenReturn(dummyId);
        when(dockerClient.startContainerCmd(dummyId)).thenReturn(startContainerCmdMock);
        doNothing().when(startContainerCmdMock).exec();
        when(dockerClient.waitContainerCmd(dummyId)).thenReturn(waitContainerMock);
        when(waitContainerMock.exec(Mockito.any(ResultCallback.Adapter.class))).thenReturn(resultCallback);

        dockerService.runContainer(dummyImageTag);

        //Assert
        verify(createContainerResponse, times(1)).getId();
        verify(startContainerCmdMock, times(1)).exec();
        verify(waitContainerMock, times(1)).exec(Mockito.any(ResultCallback.class));
    }

    @Test
    public void buildImage_ShouldExecute_With_Success() throws NullPointerException {
        //Arrange
        Set<String> dummyTagNames = Collections.singleton(dummyImageTag);
        BuildImageCmd buildImageMock = mock(BuildImageCmd.class);
        File dockerfileMock = mock(File.class);
        BuildImageResultCallback resultCallback = mock(BuildImageResultCallback.class);

        //Act
        when(dockerClient.buildImageCmd().withDockerfile(dockerfileMock).withRemove(true).withNoCache(true)
                .withTags(dummyTagNames).withBuildArg(user_name, dummyRepoUsername)
                .withBuildArg(repo_name, dummyRepoName)).thenReturn(buildImageMock);
        when(buildImageMock.exec(Mockito.any(BuildImageResultCallback.class))).thenReturn(resultCallback);
        when(resultCallback.awaitImageId()).thenReturn(dummyId);
        when(dockerClient.inspectImageCmd(dummyId).exec().getRepoTags().get(0)).thenReturn(dummyImageTag);

        String actual = dockerService.buildImage(dockerfileMock, dummyTagNames, dummyRepoUsername, dummyRepoName);

        //Assert
        Assert.assertEquals(dummyImageTag, actual);
        verify(buildImageMock, times(1)).exec(Mockito.any(ResultCallback.class));
        verify(resultCallback, times(1)).awaitImageId();
    }

    @Test
    public void getContainerLog_Should_ExecuteWithSuccess() throws InterruptedException {
        //Arrange
        LogContainerCmd logContainerCmdMock = mock(LogContainerCmd.class);
        ResultCallback.Adapter resultCallbackMock = mock(ResultCallback.Adapter.class);

        //Act
        when(dockerClient.logContainerCmd(dummyId).withStdOut(true).withStdErr(true).withTail(3))
                .thenReturn(logContainerCmdMock);
        when(logContainerCmdMock.exec(Mockito.any(ResultCallback.Adapter.class))).thenReturn(resultCallbackMock);
        when(resultCallbackMock.awaitCompletion()).thenReturn(resultCallbackMock);

        List<String> actual = dockerService.getContainerLog(dummyId);
        //Assert
        Assert.assertEquals(0, actual.size());
        verify(logContainerCmdMock, times(1)).exec(Mockito.any(ResultCallback.class));
        verify(resultCallbackMock, times(1)).awaitCompletion();
    }

    @Test
    public void deleteContainer_Should_ExecuteWithSuccess() {
        //Arrange
        RemoveContainerCmd removeContainerCmdMock = mock(RemoveContainerCmd.class);

        //Act
        when(dockerClient.removeContainerCmd(dummyId).withRemoveVolumes(true).withForce(true))
                .thenReturn(removeContainerCmdMock);
        doNothing().when(removeContainerCmdMock).exec();

        dockerService.deleteContainer(dummyId);

        //Assert
        verify(removeContainerCmdMock, times(1)).exec();
    }

    @Test
    public void generateShellArgs_Should_ReturnArgs() {
        //Arrange
        String username = "giivanov722";
        String childRepo = "docker-test-child";
        String parentRepo = "docker-test-parent";

        //Act
        List<String> actual = DockerService.generateShellArgs();

        //Assert
        Assert.assertEquals(actual.get(0), username);
        Assert.assertEquals(actual.get(2), username);
        Assert.assertEquals(actual.get(3), childRepo);
        Assert.assertEquals(actual.get(4), parentRepo);
    }

}