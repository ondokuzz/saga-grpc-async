package com.demirsoft.apiservice.api.services.payment;

import reactor.core.publisher.Mono;

public interface PaymentService {
    Mono<PaymentResponse> charge(PaymentRequest paymentRequest);

    Mono<PaymentResponse> rollback(PaymentRequest paymentRequest);
}
