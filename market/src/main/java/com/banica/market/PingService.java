package com.banica.market;

import com.banica.messages.Ping;
import com.banica.messages.PingServiceGrpc;
import com.banica.messages.Pong;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public final class PingService extends PingServiceGrpc.PingServiceImplBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(PingService.class);

    @Override
    public void sendPing(final Ping ping, final StreamObserver<Pong> responseObserver) {
        LOGGER.info("Ping - {}", ping);
        final Pong pong = Pong.newBuilder().setId(ping.getId()).build();
        responseObserver.onNext(pong);
        responseObserver.onCompleted();
        LOGGER.info("Pong - {}", pong);
    }

}
