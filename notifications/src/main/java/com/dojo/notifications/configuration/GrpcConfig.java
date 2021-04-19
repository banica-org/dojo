package com.dojo.notifications.configuration;

import com.codenjoy.dojo.EventServiceGrpc;
import com.codenjoy.dojo.LeaderboardServiceGrpc;
import com.codenjoy.dojo.UserDetailsServiceGrpc;
import com.dojo.codeexecution.DockerServiceGrpc;
import com.dojo.common.GrpcChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@ConfigurationProperties
@Component
public class GrpcConfig {

    private final GrpcChannel codenjoyChannel;
    private final GrpcChannel gameServerChannel;

    public GrpcConfig(@Value("${grpc.codenjoy.host}") String host, @Value("${grpc.codenjoy.port}") int port, @Value("${grpc.game.server.host}") String gameServerHost, @Value("${grpc.game.server.port}") int gameServerPort) {
        this.gameServerChannel = new GrpcChannel(gameServerHost, gameServerPort);
        this.codenjoyChannel = new GrpcChannel(host, port);
    }

    @Bean
    public LeaderboardServiceGrpc.LeaderboardServiceBlockingStub getLeaderboardBlockingStub() {
        return LeaderboardServiceGrpc.newBlockingStub(this.codenjoyChannel.getManagedChannel());
    }

    @Bean
    public LeaderboardServiceGrpc.LeaderboardServiceStub getLeaderboardStub() {
        return LeaderboardServiceGrpc.newStub(this.codenjoyChannel.getManagedChannel());
    }

    @Bean
    public UserDetailsServiceGrpc.UserDetailsServiceBlockingStub getUserDetailsBlockingStub() {
        return UserDetailsServiceGrpc.newBlockingStub(this.codenjoyChannel.getManagedChannel());
    }

    @Bean
    public EventServiceGrpc.EventServiceBlockingStub getEventBlockingStub() {
        return EventServiceGrpc.newBlockingStub(this.codenjoyChannel.getManagedChannel());
    }

    @Bean
    public DockerServiceGrpc.DockerServiceStub getDockerStub() {
        return DockerServiceGrpc.newStub(this.gameServerChannel.getManagedChannel());
    }
}
