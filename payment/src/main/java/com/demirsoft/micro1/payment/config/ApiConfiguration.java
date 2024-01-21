package com.demirsoft.micro1.payment.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.demirsoft.micro1.payment.grpc.GrpcPaymentServiceGrpc;
import com.demirsoft.micro1.payment.grpc.GrpcPaymentServiceGrpc.GrpcPaymentServiceBlockingStub;
import com.demirsoft.micro1.payment.grpc.impl.PaymentServiceGrpcServer;
import com.demirsoft.micro1.payment.grpc.impl.PaymentServiceGrpcServerManager;

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
    GrpcPaymentServiceBlockingStub createBlockingGrpc(ManagedChannel channel) {
        return GrpcPaymentServiceGrpc.newBlockingStub(channel);

    }

    @Bean
    PaymentServiceGrpcServer createPaymentServiceGrpcServer() {
        logger.info("creating payment grpc server bean");
        return new PaymentServiceGrpcServer();
    }

    @Bean
    public CommandLineRunner myCommandLineRunner(PaymentServiceGrpcServerManager greeterGrpcServerStarter) {

        return args -> {
            System.out.println("Non-web application is running...");
            greeterGrpcServerStarter.awaitTermination();
        };
    }

}
