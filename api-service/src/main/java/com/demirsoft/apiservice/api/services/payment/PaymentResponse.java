package com.demirsoft.apiservice.api.services.payment;

import com.demirsoft.apiservice.api.saga.TaskResponse;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@Getter
@RequiredArgsConstructor
final public class PaymentResponse implements TaskResponse {
        private final Integer orderId;
        private final Integer userId;
        private final Integer productId;
        private final Integer productCount;
        private final Double totalPrice;
        private final PaymentStatus paymentStatus;
}
