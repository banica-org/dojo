package com.banica.common;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;

public final class GrpcChannel {

    private static final Logger LOGGER = LoggerFactory.getLogger(GrpcChannel.class);

    private final ManagedChannel managedChannel;

    public GrpcChannel(final String host, final int port) {
        LOGGER.info("Managed channel started on address {}:{}...", host, port);
        this.managedChannel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .directExecutor()
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
