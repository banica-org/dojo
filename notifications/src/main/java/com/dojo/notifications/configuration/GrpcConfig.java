package com.dojo.notifications.configuration;

import com.codenjoy.dojo.EventServiceGrpc;
import com.codenjoy.dojo.LeaderboardServiceGrpc;
import com.codenjoy.dojo.UserDetailsServiceGrpc;
import com.dojo.common.GrpcChannel;
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
    public LeaderboardServiceGrpc.LeaderboardServiceBlockingStub getLeaderboardBlockingStub() {
        return LeaderboardServiceGrpc.newBlockingStub(this.grpcChannel.getManagedChannel());
    }

    @Bean
    public LeaderboardServiceGrpc.LeaderboardServiceStub getLeaderboardStub() {
        return LeaderboardServiceGrpc.newStub(this.grpcChannel.getManagedChannel());
    }

    @Bean
    public UserDetailsServiceGrpc.UserDetailsServiceBlockingStub getUserDetailsBlockingStub() {
        return UserDetailsServiceGrpc.newBlockingStub(this.grpcChannel.getManagedChannel());
    }

    @Bean
    public EventServiceGrpc.EventServiceBlockingStub getEventBlockingStub() {
        return EventServiceGrpc.newBlockingStub(this.grpcChannel.getManagedChannel());
    }
}
