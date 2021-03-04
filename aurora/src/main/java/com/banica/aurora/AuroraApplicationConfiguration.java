package com.banica.aurora;

import com.banica.common.GrpcChannel;
import com.banica.common.ShutdownManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuroraApplicationConfiguration {

    @Bean
    ShutdownManager getShutdownManager(final ApplicationContext applicationContext) {
        return new ShutdownManager(applicationContext);
    }

    @Bean
    GrpcChannel getGrpcChannel(
            @Value("${grpc.channel.host}") final String host,
            @Value("${grpc.channel.port}") final int port) {
        return new GrpcChannel(host, port);
    }

}
