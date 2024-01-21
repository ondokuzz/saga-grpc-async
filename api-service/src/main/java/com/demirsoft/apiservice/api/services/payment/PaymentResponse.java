package com.demirsoft.apiservice.api.services.payment;

public record PaymentResponse(
        Integer orderId,
        Integer userId,
        Integer productId,
        Integer productCount,
        Double totalPrice,
        PaymentStatus paymentStatus) {
}
