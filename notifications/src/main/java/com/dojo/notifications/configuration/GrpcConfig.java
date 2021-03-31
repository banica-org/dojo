package com.dojo.notifications.configuration;

import com.dojo.apimock.ApiMockServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@ConfigurationProperties
@Component
public class GrpcConfig {

    private final ManagedChannel managedChannel;

    public GrpcConfig(@Value("${grpc.server.host}") String host, @Value("${grpc.server.port}") int port) {
        this.managedChannel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .keepAliveTime(2, TimeUnit.MINUTES)
                .build();
    }

    @Bean
    public ApiMockServiceGrpc.ApiMockServiceBlockingStub getApiMockBlockingStub() {
        return ApiMockServiceGrpc.newBlockingStub(this.managedChannel);
    }

    @Bean
    public ApiMockServiceGrpc.ApiMockServiceStub getApiMockStub() {
        return ApiMockServiceGrpc.newStub(this.managedChannel);
    }
}
