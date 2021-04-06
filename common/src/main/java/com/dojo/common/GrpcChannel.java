package com.dojo.common;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

public final class GrpcChannel {

    private static final Logger LOGGER = LoggerFactory.getLogger(GrpcChannel.class);

    private final ManagedChannel managedChannel;

    public GrpcChannel(final String host, final int port) {
        LOGGER.info("Managed channel started on address {}:{}...", host, port);
        this.managedChannel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .keepAliveTime(2, TimeUnit.MINUTES)
                .build();
    }

    public ManagedChannel getManagedChannel() {
        return managedChannel;
    }

    @PreDestroy
    private void stop() {
        managedChannel.shutdownNow();
        LOGGER.info("Managed channel stopped!");
    }

}
