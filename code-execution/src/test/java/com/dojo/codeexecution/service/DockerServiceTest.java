package com.dojo.codeexecution.service;

import com.dojo.codeexecution.config.github.GitConfigProperties;
import com.dojo.codeexecution.config.docker.DockerConfigProperties;
import com.dojo.codeexecution.service.docker.DockerServiceImpl;
import com.dojo.codeexecution.service.grpc.handler.DockerEventUpdateHandler;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerCmd;
import com.github.dockerjava.api.command.InspectContainerResponse;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class DockerServiceTest {

    private static String dummyImageTag;
    private static String user_name;
    private static String repo_name;
    private static String dummyRepoUsername;
    private static String dummyRepoName;
    private static String dummyId;
    private static String dummyContainerStatus;
    @InjectMocks
    private DockerServiceImpl dockerServiceImpl;
    @Mock
    private DockerConfigProperties dockerConfigProperties;
    @Mock
    private GitConfigProperties gitConfigProperties;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DockerClient dockerClient;
    @Mock
    private DockerEventUpdateHandler dockerEventUpdateHandler;
    @Mock
    private ExecutorService singleThreadExecutor;

    @BeforeClass
    public static void setUp() {
        dummyImageTag = "dummy-tag:latest";
        user_name = "user_name";
        repo_name = "repo_name";
        dummyRepoUsername = "dummyuser";
        dummyRepoName = "dummy-reponame";
        dummyId = "1234";
        dummyContainerStatus = "dummy-status";
    }

    //    @Ignore
    @Test
    public void runContainer() {
        //Arrange
        CreateContainerResponse createContainerResponseMock = mock(CreateContainerResponse.class);
        StartContainerCmd startContainerCmdMock = mock(StartContainerCmd.class);
        WaitContainerCmd waitContainerMock = mock(WaitContainerCmd.class);
        ResultCallback.Adapter resultCallback = mock(ResultCallback.Adapter.class);
        InspectContainerCmd inspectContainerCmdMock = mock(InspectContainerCmd.class);
        InspectContainerResponse inspectContainerResponseMock = mock(InspectContainerResponse.class);
        InspectContainerResponse.ContainerState containerStateMock =
                mock(InspectContainerResponse.ContainerState.class);

        //Act
        when(dockerClient.createContainerCmd(dummyImageTag).withCmd(any(List.class))
                .exec()).thenReturn(createContainerResponseMock);
        when(createContainerResponseMock.getId()).thenReturn(dummyId);
        when(dockerClient.startContainerCmd(dummyId)).thenReturn(startContainerCmdMock);
        doNothing().when(startContainerCmdMock).exec();
        when(dockerClient.inspectContainerCmd(dummyId)).thenReturn(inspectContainerCmdMock);
        when(inspectContainerCmdMock.exec()).thenReturn(inspectContainerResponseMock);
        when(inspectContainerResponseMock.getState()).thenReturn(containerStateMock);
        when(containerStateMock.getStatus()).thenReturn(dummyContainerStatus);
        when(dockerClient.waitContainerCmd(dummyId)).thenReturn(waitContainerMock);
        when(waitContainerMock.exec(any(ResultCallback.Adapter.class))).thenReturn(resultCallback);

        dockerServiceImpl.runContainer(dummyImageTag, dummyRepoUsername);

        //Assert
        verify(createContainerResponseMock, times(1)).getId();
        verify(startContainerCmdMock, times(1)).exec();
        verify(dockerClient, times(1)).inspectContainerCmd(dummyId);
        verify(inspectContainerCmdMock, times(1)).exec();
        verify(inspectContainerResponseMock, times(1)).getState();
        verify(containerStateMock, times(1)).getStatus();
        verify(waitContainerMock, times(1)).exec(any(ResultCallback.class));
    }

    @Test
    public void buildImage_ShouldExecute_With_Success() {
        //Arrange
        when(singleThreadExecutor.submit(any(Runnable.class))).thenReturn(Mockito.mock(Future.class));

        //Act
        dockerServiceImpl.buildImage();

        //Assert
        verify(singleThreadExecutor, times(1)).submit(any(Runnable.class));
    }

    @Test
    public void getContainerLog_Should_ExecuteWithSuccess() throws InterruptedException {
        //Arrange
        List<String> dummyLogs = new ArrayList<>();
        LogContainerCmd logContainerCmdMock = mock(LogContainerCmd.class);
        ResultCallback.Adapter resultCallbackMock = mock(ResultCallback.Adapter.class);

        //Act
        when(dockerClient.logContainerCmd(dummyId).withStdOut(true).withStdErr(true))
                .thenReturn(logContainerCmdMock);
        when(logContainerCmdMock.exec(any(ResultCallback.Adapter.class))).thenReturn(resultCallbackMock);
        when(resultCallbackMock.awaitCompletion()).thenReturn(resultCallbackMock);

        List<String> actual = dockerServiceImpl.getContainerLog(dummyId, dummyLogs);
        //Assert
        Assert.assertEquals(0, actual.size());
        verify(logContainerCmdMock, times(1)).exec(any(ResultCallback.class));
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

        dockerServiceImpl.deleteContainerWithForce(dummyId);

        //Assert
        verify(removeContainerCmdMock, times(1)).exec();
    }

}