package com.demirsoft.micro1.payment.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.demirsoft.micro1.payment.grpc.impl.PaymentServiceGrpcServer;
import com.demirsoft.micro1.payment.grpc.impl.PaymentServiceGrpcServerManager;

@Configuration
public class ApiConfiguration {
    private final Logger logger = LogManager.getLogger(getClass());

    @Bean
    PaymentServiceGrpcServer createPaymentServiceGrpcServer() {
        logger.info("creating payment grpc server bean");
        return new PaymentServiceGrpcServer();
    }

    @Bean
    public CommandLineRunner myCommandLineRunner(PaymentServiceGrpcServerManager paymentGrpcServerStarter) {

        return args -> {
            System.out.println("Non-web application is running...");
            paymentGrpcServerStarter.awaitTermination();
        };
    }

}
