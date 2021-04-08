package com.dojo.notifications.configuration;

import com.dojo.apimock.ApiMockLeaderboardServiceGrpc;
import com.dojo.apimock.ApiMockUserDetailsServiceGrpc;
import com.dojo.common.GrpcChannel;
import io.grpc.ManagedChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@ConfigurationProperties
@Component
public class GrpcConfig {

    private final GrpcChannel grpcChannel;

    public GrpcConfig(@Value("${grpc.server.host}") String host, @Value("${grpc.server.port}") int port) {
        this.grpcChannel = new GrpcChannel(host, port);
    }

    @Bean
    public ApiMockLeaderboardServiceGrpc.ApiMockLeaderboardServiceBlockingStub getLeaderboardBlockingStub() {
        return ApiMockLeaderboardServiceGrpc.newBlockingStub(this.grpcChannel.getManagedChannel());
    }

    @Bean
    public ApiMockLeaderboardServiceGrpc.ApiMockLeaderboardServiceStub getLeaderboardStub() {
        return ApiMockLeaderboardServiceGrpc.newStub(this.grpcChannel.getManagedChannel());
    }

    @Bean
    public ApiMockUserDetailsServiceGrpc.ApiMockUserDetailsServiceBlockingStub getUserDetailsBlockingStub() {
        return ApiMockUserDetailsServiceGrpc.newBlockingStub(this.grpcChannel.getManagedChannel());
    }
}
