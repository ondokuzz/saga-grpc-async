package com.demirsoft.apiservice.api.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.demirsoft.apiservice.api.grpc.InventoryServiceGrpcClient;
import com.demirsoft.apiservice.api.grpc.PaymentServiceGrpcClient;
import com.demirsoft.apiservice.api.services.inventory.InventoryService;
import com.demirsoft.apiservice.api.services.order.OrderService;
import com.demirsoft.apiservice.api.services.order.OrderServiceImpl;
import com.demirsoft.apiservice.api.services.payment.PaymentService;
import com.demirsoft.micro1.payment.grpc.GrpcInventoryServiceGrpc;
import com.demirsoft.micro1.payment.grpc.GrpcInventoryServiceGrpc.GrpcInventoryServiceFutureStub;
import com.demirsoft.micro1.payment.grpc.GrpcPaymentServiceGrpc;
import com.demirsoft.micro1.payment.grpc.GrpcPaymentServiceGrpc.GrpcPaymentServiceFutureStub;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

@Configuration
public class ApiConfiguration {
    private final Logger logger = LogManager.getLogger(getClass());

    @Autowired
    private PaymentServiceProperties paymentServiceProperties;

    @Autowired
    private InventoryServiceProperties inventoryServiceProperties;

    @Bean
    @Qualifier("Payment")
    ManagedChannel createPaymentGrpcChannel() {
        logger.info("creating payment grpc channel for {} {}", paymentServiceProperties.getGrpcServerHost(),
                paymentServiceProperties.getGrpcServerPort());

        return ManagedChannelBuilder
                .forAddress(paymentServiceProperties.getGrpcServerHost(), paymentServiceProperties.getGrpcServerPort())
                .usePlaintext()
                .build();
    }

    @Bean
    @Qualifier("Inventory")
    ManagedChannel createInventoryGrpcChannel() {
        logger.info("creating inventory grpc channel for {} {}",
                inventoryServiceProperties.getGrpcServerHost(),
                inventoryServiceProperties.getGrpcServerPort());

        return ManagedChannelBuilder
                .forAddress(inventoryServiceProperties.getGrpcServerHost(),
                        inventoryServiceProperties.getGrpcServerPort())
                .usePlaintext()
                .build();
    }

    @Bean
    GrpcPaymentServiceFutureStub createPaymentFutureStub(@Qualifier("Payment") ManagedChannel channel) {
        return GrpcPaymentServiceGrpc.newFutureStub(channel);

    }

    @Bean
    GrpcInventoryServiceFutureStub createInventoryFutureStub(@Qualifier("Inventory") ManagedChannel channel) {
        return GrpcInventoryServiceGrpc.newFutureStub(channel);

    }

    @Bean
    PaymentService createPaymentService(GrpcPaymentServiceFutureStub paymentGrpcStub) {
        logger.info("creating payment grpc client");
        return new PaymentServiceGrpcClient(paymentGrpcStub);
    }

    @Bean
    InventoryService createInventoryService(GrpcInventoryServiceFutureStub inventoryGrpcStub) {
        logger.info("creating inventory grpc client");
        return new InventoryServiceGrpcClient(inventoryGrpcStub);
    }

    @Bean
    OrderService createOrderService(PaymentService paymentService, InventoryService inventoryService) {
        logger.info("creating OrderServiceImpl");
        return new OrderServiceImpl(paymentService, inventoryService);
    }
}
