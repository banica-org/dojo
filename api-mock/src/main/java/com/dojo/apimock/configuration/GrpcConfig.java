package com.dojo.apimock.configuration;

import com.dojo.apimock.service.LeaderboardService;
import com.dojo.apimock.service.UserDetailsService;
import com.dojo.common.GrpcServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@ConfigurationProperties
@Component
public class GrpcConfig {

    private final GrpcServer grpcServer;

    public GrpcConfig(@Value("${executor.pool.size}") final int applicationExecutorPoolSize,
                      @Value("${grpc.server.port}") final int port,
                      final LeaderboardService leaderboardService,
                      final UserDetailsService userDetailsService) {
        this.grpcServer = new GrpcServer(applicationExecutorPoolSize, port, leaderboardService, userDetailsService);
    }

    @Bean
    public GrpcServer getGrpcServer () {
        return grpcServer;
    }

}
