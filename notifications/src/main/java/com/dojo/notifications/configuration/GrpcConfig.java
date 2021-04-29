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

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties
@Component
public class GrpcConfig {

    private final GrpcChannel codenjoyChannel;
    private final Map<String, GrpcChannel> gameServerChannels;

    public GrpcConfig(@Value("${grpc.codenjoy.host}") String host, @Value("${grpc.codenjoy.port}") int port) {
        this.codenjoyChannel = new GrpcChannel(host, port);
        this.gameServerChannels = new HashMap<>();
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

    public DockerServiceGrpc.DockerServiceStub getDockerServiceStub(String gameServerUrl) {
        GrpcChannel channel = getGrpcChannel(gameServerUrl);
        return DockerServiceGrpc.newStub(channel.getManagedChannel());
    }

    public DockerServiceGrpc.DockerServiceBlockingStub getDockerServiceBlockingStub(String gameServerUrl) {
        GrpcChannel channel = getGrpcChannel(gameServerUrl);
        return DockerServiceGrpc.newBlockingStub(channel.getManagedChannel());
    }

    private GrpcChannel getGrpcChannel(String gameServerUrl) {
        GrpcChannel channel;
        if (gameServerChannels.containsKey(gameServerUrl)) {
            channel = gameServerChannels.get(gameServerUrl);
        } else {
            channel = buildChannel(gameServerUrl);
            gameServerChannels.put(gameServerUrl, channel);
        }
        return channel;
    }

    private GrpcChannel buildChannel(String gameServerUrl) {
        String[] urlData = gameServerUrl.split(":");
        String host = urlData[0];
        int port = Integer.parseInt(urlData[1]);

        return new GrpcChannel(host, port);
    }
}
