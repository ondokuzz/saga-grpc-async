package com.demirsoft.apiservice.api.services.payment;

import java.time.Duration;

import com.demirsoft.apiservice.api.saga.SagaTask;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@Log4j2
public class PaymentTask implements SagaTask<PaymentResponse> {
    private static final Duration TIMEOUT = Duration.ofSeconds(1);

    private final PaymentService paymentService;
    private final PaymentRequest paymentRequest;

    public PaymentTask(PaymentService paymentService, PaymentRequest paymentRequest) {
        this.paymentService = paymentService;
        this.paymentRequest = paymentRequest;
    }

    @Override
    public Mono<PaymentResponse> perform() {
        log.debug("perform: payment task");
        return this.paymentService.charge(paymentRequest);
    }

    @Override
    public Mono<PaymentResponse> rollback() {
        log.debug("rollback: payment task");
        return this.paymentService.rollback(paymentRequest);

    }

    @Override
    public Duration timeout() {
        return TIMEOUT;
    }

}