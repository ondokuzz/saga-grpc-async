package com.demirsoft.apiservice.api.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import com.demirsoft.apiservice.api.grpc.PaymentServiceGrpcClient;
import com.demirsoft.apiservice.api.services.inventory.InventoryService;
import com.demirsoft.apiservice.api.services.inventory.InventoryServiceImpl;
import com.demirsoft.apiservice.api.services.order.OrderService;
import com.demirsoft.apiservice.api.services.order.OrderServiceImpl;
import com.demirsoft.apiservice.api.services.payment.PaymentService;
import com.demirsoft.apiservice.api.services.payment.PaymentServiceImpl;
import com.demirsoft.micro1.payment.grpc.GrpcInventoryServiceGrpc;
import com.demirsoft.micro1.payment.grpc.GrpcInventoryServiceGrpc.GrpcInventoryServiceFutureStub;
import com.demirsoft.micro1.payment.grpc.GrpcPaymentServiceGrpc;
import com.demirsoft.micro1.payment.grpc.GrpcPaymentServiceGrpc.GrpcPaymentServiceFutureStub;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.log4j.Log4j2;

@Configuration
@Log4j2
public class ApiConfiguration {

    @Autowired
    private PaymentServiceProperties paymentServiceProperties;

    @Autowired
    private InventoryServiceProperties inventoryServiceProperties;

    @Bean
    @Qualifier("Payment")
    ManagedChannel createPaymentGrpcChannel(PaymentServiceProperties paymentServiceProperties) {
        String host = paymentServiceProperties.getGrpcServerHost();
        Integer port = paymentServiceProperties.getGrpcServerPort();

        log.info("creating payment grpc channel for {} {}", host, port);

        return createGrpcChannel(host, port);
    }

    @Bean
    @Qualifier("Inventory")
    ManagedChannel createInventoryGrpcChannel(InventoryServiceProperties inventoryServiceProperties) {
        String host = inventoryServiceProperties.getGrpcServerHost();
        Integer port = inventoryServiceProperties.getGrpcServerPort();

        log.info("creating inventory grpc channel for {} {}", host, port);

        return createGrpcChannel(host, port);
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
    PaymentServiceGrpcClient createGrpcPaymentService(
            GrpcPaymentServiceFutureStub paymentGrpcStub,
            PaymentServiceProperties paymentServiceProperties) {
        log.info("creating payment grpc client");
        return new PaymentServiceGrpcClient(paymentGrpcStub, paymentServiceProperties);
    }

    @Bean
    @Qualifier("InventoryService")
    WebClient createWebClient(WebClient.Builder webClientBuilder,
            InventoryServiceProperties inventoryServiceProperties) {
        return webClientBuilder.baseUrl(inventoryServiceProperties.getHostUrl()).build();
    }

    @Bean
    PaymentService createPaymentService(
            PaymentServiceGrpcClient paymentServiceGrpcClient,
            PaymentServiceProperties paymentServiceProperties) {
        log.info("creating payment grpc client");
        return new PaymentServiceImpl(paymentServiceGrpcClient, paymentServiceProperties);
    }

    @Bean
    InventoryService createInventoryService(
            InventoryServiceProperties inventoryServiceProperties,
            @Qualifier("InventoryService") WebClient webClient) {
        log.info("creating inventory grpc client");
        return new InventoryServiceImpl(inventoryServiceProperties, webClient);
    }

    @Bean
    OrderService createOrderService(PaymentService paymentService, InventoryService inventoryService) {
        log.info("creating OrderService");
        return new OrderServiceImpl(paymentService, inventoryService);
    }

    private ManagedChannel createGrpcChannel(String host, Integer port) {
        return ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
    }
}
