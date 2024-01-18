package com.demirsoft.controller.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.demirsoft.controller.grpc.GreeterGrpcServer;
import com.demirsoft.controller.grpc.GreeterGrpcServerManager;
import com.demirsoft.greeter.GreeterGrpc;
import com.demirsoft.greeter.GreeterGrpc.GreeterBlockingStub;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

@Configuration
public class ApiConfiguration {
    private final Logger logger = LogManager.getLogger(getClass());

    @Autowired
    private AppProperties appProperties;

    @Bean
    ManagedChannel createGrpcChannel() {
        logger.info("creating grpc channel for {} {}", appProperties.getGrpcServerHost(),
                appProperties.getGrpcServerPort());

        return ManagedChannelBuilder.forAddress(appProperties.getGrpcServerHost(), appProperties.getGrpcServerPort())
                .usePlaintext()
                .build();
    }

    @Bean
    GreeterBlockingStub createBlockingGrpc(ManagedChannel channel) {
        return GreeterGrpc.newBlockingStub(channel);

    }

    @Bean
    GreeterGrpcServer GreeterGrpcServer() {
        logger.info("creating grpc server bean");
        return new GreeterGrpcServer();
    }

    @Bean
    public CommandLineRunner myCommandLineRunner(GreeterGrpcServerManager greeterGrpcServerStarter) {

        return args -> {
            System.out.println("Non-web application is running...");
            greeterGrpcServerStarter.awaitTermination();
        };
    }

}
