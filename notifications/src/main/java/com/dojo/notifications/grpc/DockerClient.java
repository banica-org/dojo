package com.dojo.notifications.grpc;

import com.dojo.codeexecution.ContainerRequest;
import com.dojo.codeexecution.ContainerResponse;
import com.dojo.codeexecution.DockerServiceGrpc;
import com.dojo.codeexecution.ImageRequest;
import com.dojo.codeexecution.ImageResponse;
import com.dojo.codeexecution.StopRequest;
import com.dojo.codeexecution.StopResponse;
import com.dojo.codeexecution.TestResultRequest;
import com.dojo.codeexecution.TestResultResponse;
import com.dojo.notifications.configuration.GrpcConfig;
import com.dojo.notifications.model.contest.Contest;
import com.dojo.notifications.model.docker.Container;
import com.dojo.notifications.model.docker.Image;
import com.dojo.notifications.model.docker.TestResults;
import com.dojo.notifications.model.notification.enums.NotificationType;
import com.dojo.notifications.service.DockerNotifierService;
import com.dojo.notifications.service.EventService;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DockerClient {
    private static final String RESPONSE_MESSAGE = "Response: {}";
    private static final String ERROR_MESSAGE = "Unable to request";
    private static final String COMPLETED_MESSAGE = "Completed.";

    private static final String NOTIFICATIONS_STARTED_MESSAGE = "Docker notifications started for contest {}";
    private static final String NOTIFICATION_STOPPED_MESSAGE = "Docker notifications stopped for contest {} {}";

    private static final String IMAGE_MESSAGE = "Notification about image build:\nContest: %s";
    private static final String CONTAINER_MESSAGE = "Notification about container run:\nContest: %s";
    private static final String TEST_RESULTS_MESSAGE = "Notification about test results:\nContest: %s";

    private static final String SERVER_ID = "NotificationServer";

    private static final String EXECUTION_EMPTY = "";
    private static final String EXECUTION_SUCCESS = "success";
    private static final String EXECUTION_FAILURE = "failure";

    private static final Logger LOGGER = LoggerFactory.getLogger(DockerClient.class);

    private final Map<Contest, DockerServiceGrpc.DockerServiceStub> dockerServiceStubs;
    private final EventService eventService;
    private final DockerNotifierService dockerNotifierService;

    private final GrpcConfig grpcConfig;

    @Autowired
    public DockerClient(EventService eventService, DockerNotifierService dockerNotifierService, GrpcConfig grpcConfig) {
        this.eventService = eventService;
        this.dockerNotifierService = dockerNotifierService;
        this.grpcConfig = grpcConfig;
        this.dockerServiceStubs = new HashMap<>();
    }

    public void startDockerNotifications(Contest contest) {
        if (!dockerServiceStubs.containsKey(contest)) {
            String contestId = contest.getContestId();

            String gameServerUrl = eventService.getGameServerForContest(contestId);
            DockerServiceGrpc.DockerServiceStub dockerServiceStub = grpcConfig.getDockerServiceStub(gameServerUrl);
            dockerServiceStubs.put(contest, dockerServiceStub);

            LOGGER.info(NOTIFICATIONS_STARTED_MESSAGE, contestId);

            getImageResults(contest);
            getContainerResults(contest);
            getTestResults(contest);
        }
    }

    public void stopDockerNotifications(Contest contest) {
        String contestId = contest.getContestId();
        String gameServiceUrl = eventService.getGameServerForContest(contestId);
        DockerServiceGrpc.DockerServiceBlockingStub dockerServiceBlockingStub = grpcConfig.getDockerServiceBlockingStub(gameServiceUrl);

        StopRequest request = StopRequest.newBuilder().setId(SERVER_ID).build();
        StopResponse response = dockerServiceBlockingStub.stopNotifications(request);
        LOGGER.info(NOTIFICATION_STOPPED_MESSAGE, contestId, response);
    }

    private void getImageResults(Contest contest) {
        ImageRequest request = ImageRequest.newBuilder().setId(SERVER_ID).build();

        dockerServiceStubs
                .get(contest)
                .getImageResults(request, new StreamObserver<ImageResponse>() {
                    @Override
                    public void onNext(ImageResponse imageResponse) {
                        Image image = new Image(imageResponse.getTag(), imageResponse.getMessage());

                        //image build results are always sent to sensei
                        //not configurable
                        dockerNotifierService.notifySensei(contest, image, String.format(IMAGE_MESSAGE, contest.getContestId()), NotificationType.IMAGE);
                        LOGGER.info(RESPONSE_MESSAGE, imageResponse);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        LOGGER.error(ERROR_MESSAGE, throwable);
                    }

                    @Override
                    public void onCompleted() {
                        LOGGER.info(COMPLETED_MESSAGE);
                    }
                });
    }

    private void getContainerResults(Contest contest) {
        ContainerRequest request = ContainerRequest.newBuilder().setId(SERVER_ID).build();

        dockerServiceStubs
                .get(contest)
                .getContainerResults(request, new StreamObserver<ContainerResponse>() {
                    @Override
                    public void onNext(ContainerResponse containerResponse) {
                        Container container = getContainer(containerResponse);
                        dockerNotifierService.executeRequests(contest, container, String.format(CONTAINER_MESSAGE, contest.getContestId()));
                        LOGGER.info(RESPONSE_MESSAGE, containerResponse);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        LOGGER.error(ERROR_MESSAGE, throwable);
                    }

                    @Override
                    public void onCompleted() {
                        LOGGER.info(COMPLETED_MESSAGE);
                    }
                });
    }

    private void getTestResults(Contest contest) {
        TestResultRequest request = TestResultRequest.newBuilder().setId(SERVER_ID).build();

        dockerServiceStubs
                .get(contest)
                .getTestResults(request, new StreamObserver<TestResultResponse>() {
                    @Override
                    public void onNext(TestResultResponse testResultResponse) {
                        TestResults testResults = getTestResults(testResultResponse);
                        dockerNotifierService.executeRequests(contest, testResults, String.format(TEST_RESULTS_MESSAGE, contest.getContestId()));
                        LOGGER.info(RESPONSE_MESSAGE, testResultResponse);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        LOGGER.error(ERROR_MESSAGE, throwable);
                    }

                    @Override
                    public void onCompleted() {
                        LOGGER.info(COMPLETED_MESSAGE);
                    }
                });
    }

    private TestResults getTestResults(TestResultResponse testResultResponse) {
        String username = testResultResponse.getUsername();
        Map<String, String> results = new HashMap<>();
        testResultResponse.getFailedTestCaseList().forEach(failedTestCase -> results.put(failedTestCase.getMethodName(), failedTestCase.getExpected()));
        return new TestResults(username, results);
    }

    private Container getContainer(ContainerResponse containerResponse) {
        List<String> logs = new ArrayList<>();
        String execution = EXECUTION_EMPTY;

        if (!containerResponse.getLogList().isEmpty()) {
            String log = containerResponse.getLog(1);
            String regex = "^STDOUT: (.*?\\[.*?\\].*?)$";
            Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(log);

            while (matcher.find()) {
                logs.add(matcher.group(1));
            }
            execution = EXECUTION_FAILURE;
            if (containerResponse.getLogList().size() > 2) {
                logs.add(containerResponse.getLog(2).replaceAll("STDOUT: ", ""));
                execution = EXECUTION_SUCCESS;
            }
        }
        return new Container(containerResponse.getUsername(), containerResponse.getStatus(), execution, logs);
    }
}
