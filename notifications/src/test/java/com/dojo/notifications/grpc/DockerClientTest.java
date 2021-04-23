package com.dojo.notifications.grpc;

import com.dojo.codeexecution.ContainerRequest;
import com.dojo.codeexecution.DockerServiceGrpc;
import com.dojo.codeexecution.ImageRequest;
import com.dojo.codeexecution.StopRequest;
import com.dojo.codeexecution.TestResultRequest;
import com.dojo.notifications.configuration.GrpcConfig;
import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.service.DockerNotifierService;
import com.dojo.notifications.service.EventService;
import io.grpc.stub.StreamObserver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
public class DockerClientTest {

    private static final String SERVER_ID = "NotificationServer";
    private static final String CONTEST_ID = "1";
    private static final String GAME_SERVER = "localhost:9090";

    @Mock
    private Contest contest;

    @Mock
    private EventService eventService;
    @Mock
    private DockerNotifierService dockerNotifierService;

    @Mock
    private GrpcConfig grpcConfig;

    @Mock
    private DockerServiceGrpc.DockerServiceStub dockerServiceStub;
    @Mock
    private DockerServiceGrpc.DockerServiceBlockingStub dockerServiceBlockingStub;

    private DockerClient dockerClient;

    @Before
    public void init() {
        dockerClient = new DockerClient(eventService, dockerNotifierService, grpcConfig);
        when(contest.getContestId()).thenReturn(CONTEST_ID);
        when(eventService.getGameServerForContest(CONTEST_ID)).thenReturn(GAME_SERVER);
    }

    @Test
    public void startDockerNotificationsTest() {
        when(grpcConfig.getDockerServiceStub(GAME_SERVER)).thenReturn(dockerServiceStub);
        ImageRequest imageRequest = ImageRequest.newBuilder().setId(SERVER_ID).build();
        ContainerRequest containerRequest = ContainerRequest.newBuilder().setId(SERVER_ID).build();
        TestResultRequest testResultRequest = TestResultRequest.newBuilder().setId(SERVER_ID).build();

        dockerClient.startDockerNotifications(contest);

        verify(dockerServiceStub, times(1)).getImageResults(eq(imageRequest), any(StreamObserver.class));
        verify(dockerServiceStub, times(1)).getContainerResults(eq(containerRequest), any(StreamObserver.class));
        verify(dockerServiceStub, times(1)).getTestResults(eq(testResultRequest), any(StreamObserver.class));
    }

    @Test
    public void stopDockerNotificationsTest() {
        when(grpcConfig.getDockerServiceBlockingStub(GAME_SERVER)).thenReturn(dockerServiceBlockingStub);
        StopRequest stopRequest = StopRequest.newBuilder().setId(SERVER_ID).build();

        dockerClient.stopDockerNotifications(contest);

        verify(dockerServiceBlockingStub, times(1)).stopNotifications(stopRequest);
    }
}
