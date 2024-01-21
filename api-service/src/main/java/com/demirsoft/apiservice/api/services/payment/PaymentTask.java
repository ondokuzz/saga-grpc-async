package com.demirsoft.apiservice.api.services.payment;

import java.time.Duration;

import com.demirsoft.apiservice.api.grpc.PaymentServiceGrpcClient;
import com.demirsoft.apiservice.api.saga.SagaTask;

import reactor.core.publisher.Mono;

public class PaymentTask implements SagaTask<PaymentResponse> {

    private final PaymentService paymentService;
    private final PaymentRequest paymentRequest;

    public PaymentTask(PaymentService paymentService, PaymentRequest paymentRequest) {
        this.paymentService = paymentService;
        this.paymentRequest = paymentRequest;
    }

    @Override
    public Mono<PaymentResponse> perform() {
        System.out.println("perform: payment task");
        return this.paymentService.charge(paymentRequest);
    }

    @Override
    public Mono<PaymentResponse> rollback() {
        System.out.println("rollback: payment task");
        return this.paymentService.rollback(paymentRequest);

    }

    @Override
    public void timeout() {
    }

}