package com.dojo.notifications.configuration;

import com.dojo.apimock.ApiMockServiceGrpc;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@ConfigurationProperties
@Component
public class Configuration {
    private String leaderboardApi;
    private String userDetailsApi;
    private int threadPoolSchedulePeriodSeconds;
    private int threadPoolSize;

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public void setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }

    public void setLeaderboardApi(String leaderboardApi) {
        this.leaderboardApi = leaderboardApi;
    }

    public int getThreadPoolSchedulePeriodSeconds() {
        return threadPoolSchedulePeriodSeconds;
    }

    public void setThreadPoolSchedulePeriodSeconds(int threadPoolSchedulePeriodSeconds) {
        this.threadPoolSchedulePeriodSeconds = threadPoolSchedulePeriodSeconds;
    }

    public String getLeaderboardApi() {
        return leaderboardApi;
    }

    public String getUserDetailsApi() {
        return userDetailsApi;
    }

    public void setUserDetailsApi(String userDetailsApi) {
        this.userDetailsApi = userDetailsApi;
    }

    @Bean
    public ApiMockServiceGrpc.ApiMockServiceBlockingStub getApiMockBlockingStub(@Value("${grpc.server.host}") String host, @Value("${grpc.server.port}") final int port) {
        return ApiMockServiceGrpc.newBlockingStub(ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .keepAliveTime(2, TimeUnit.MINUTES)
                .build());
    }
}

