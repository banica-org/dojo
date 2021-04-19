package com.dojo.codeexecution.config;

import com.dojo.codeexecution.service.grpc.DockerService;
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
                      final DockerService dockerService) {
        this.grpcServer = new GrpcServer(applicationExecutorPoolSize, port, dockerService);
    }

    @Bean
    public GrpcServer getGrpcServer () {
        return grpcServer;
    }

}

