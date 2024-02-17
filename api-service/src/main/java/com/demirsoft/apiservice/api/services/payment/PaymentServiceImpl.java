package com.demirsoft.apiservice.api.services.payment;

import com.demirsoft.apiservice.api.config.PaymentServiceProperties;
import com.demirsoft.apiservice.api.grpc.PaymentServiceGrpcClient;

import reactor.core.publisher.Mono;

public class PaymentServiceImpl implements PaymentService {

    private final PaymentServiceGrpcClient paymentServiceGrpcClient;
    private final PaymentServiceProperties paymentServiceProperties;

    public PaymentServiceImpl(
            PaymentServiceGrpcClient paymentServiceGrpcClient,
            PaymentServiceProperties paymentServiceProperties) {
        this.paymentServiceGrpcClient = paymentServiceGrpcClient;
        this.paymentServiceProperties = paymentServiceProperties;
    }

    @Override
    public Mono<PaymentResponse> charge(PaymentRequest paymentRequest) {
        return paymentServiceGrpcClient.charge(paymentRequest);
    }

    @Override
    public Mono<PaymentResponse> rollback(PaymentRequest paymentRequest) {
        return paymentServiceGrpcClient.rollback(paymentRequest);
    }

}
