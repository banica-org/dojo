package com.banica.aurora;

import com.banica.common.GrpcChannel;
import com.banica.messages.Ping;
import com.banica.messages.PingServiceGrpc;
import com.banica.messages.Pong;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public final class PingClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(PingClient.class);

    private final PingServiceGrpc.PingServiceBlockingStub pingServiceStub;

    private final AtomicInteger pingCounter;

    private final ScheduledExecutorService scheduledExecutorService;

    public PingClient(final GrpcChannel channel) {
        pingServiceStub = PingServiceGrpc.newBlockingStub(channel.getManagedChannel());
        pingCounter = new AtomicInteger(1);
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    }

    @PostConstruct
    private void start() {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            final Ping ping = Ping.newBuilder().setId(pingCounter.getAndIncrement()).build();
            LOGGER.info("Ping - {}", ping);
            try {
                final Pong pong = pingServiceStub.sendPing(ping);
                LOGGER.info("Pong - {}", pong);
            } catch (final StatusRuntimeException exception) {
                LOGGER.error("Unable to get pong for ping - {}", ping);
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    @PreDestroy
    private void destory() {
        scheduledExecutorService.shutdownNow();
    }

}
