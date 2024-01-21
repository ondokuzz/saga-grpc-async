package com.demirsoft.apiservice.api.services.payment;

public record PaymentRequest(Integer orderId, Integer userId, Integer productId, Integer productCount, Double price) {
}
