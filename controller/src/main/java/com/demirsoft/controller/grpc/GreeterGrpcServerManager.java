package com.demirsoft.controller.grpc;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.demirsoft.controller.config.AppProperties;

import io.grpc.Server;
import io.grpc.ServerBuilder;

@Component
public class GreeterGrpcServerManager {

    private final Logger logger = LogManager.getLogger(getClass());

    private final Server server;

    public GreeterGrpcServerManager(AppProperties appProperties) {
        logger.info("initing grpc server for {}", appProperties.getGrpcServerPort());
        server = ServerBuilder
                .forPort(appProperties.getGrpcServerPort())
                .addService(new GreeterGrpcServer()).build();

        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void awaitTermination() {
        try {
            server.awaitTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            logger.info("shutting down grpc server");
            server.shutdown().awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*
     * public void start() throws IOException, InterruptedException {
     * Executors.newSingleThreadExecutor().submit(() -> {
     * try {
     * this.init();
     * } catch (IOException e) {
     * e.printStackTrace();
     * } catch (InterruptedException e) {
     * e.printStackTrace();
     * }
     * });
     * }
     */

}
