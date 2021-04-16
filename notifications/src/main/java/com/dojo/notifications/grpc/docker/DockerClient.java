package com.dojo.notifications.grpc.docker;

import com.dojo.codeexecution.ContainerRequest;
import com.dojo.codeexecution.ContainerResponse;
import com.dojo.codeexecution.DockerServiceGrpc;
import com.dojo.codeexecution.ImageRequest;
import com.dojo.codeexecution.ImageResponse;
import com.dojo.codeexecution.TestResultRequest;
import com.dojo.codeexecution.TestResultResponse;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class DockerClient {
    private static final String RESPONSE_MESSAGE = "Response: {}";
    private static final String ERROR_MESSAGE = "Unable to request";
    private static final String COMPLETED_MESSAGE = "Completed.";

    private static final Logger LOGGER = LoggerFactory.getLogger(DockerClient.class);

    private final DockerServiceGrpc.DockerServiceStub dockerServiceStub;

    @Autowired
    public DockerClient(DockerServiceGrpc.DockerServiceStub dockerServiceStub) {
        this.dockerServiceStub = dockerServiceStub;
    }

    public void getImageResults() {
        ImageRequest request = ImageRequest.newBuilder().build();

        dockerServiceStub.getImageResults(request, new StreamObserver<ImageResponse>() {
            @Override
            public void onNext(ImageResponse imageResponse) {
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

    public void getContainerResults() {
        ContainerRequest request = ContainerRequest.newBuilder().build();

        dockerServiceStub.getContainerResults(request, new StreamObserver<ContainerResponse>() {
            @Override
            public void onNext(ContainerResponse containerResponse) {
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

    public void getTestResults() {
        TestResultRequest request = TestResultRequest.newBuilder().build();

        dockerServiceStub.getTestResults(request, new StreamObserver<TestResultResponse>() {
            @Override
            public void onNext(TestResultResponse testResultResponse) {
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

    @PostConstruct
    public void startDockerNotifications() {
        getImageResults();
        getContainerResults();
        getTestResults();
    }
}
