package com.banica.market;

import com.banica.common.GrpcServer;
import com.banica.common.ShutdownManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MarketApplicationConfiguration {

    @Bean
    ShutdownManager getShutdownManager(final ApplicationContext applicationContext) {
        return new ShutdownManager(applicationContext);
    }

    @Bean
    GrpcServer getGrpcServer(@Value("${grpc.server.port}") final int port, final PingService pingService) {
        return new GrpcServer(port, pingService);
    }

}
