package com.demirsoft.apiservice.api.services.order;

public record OrderResponse(
        Integer orderId,
        Integer userId,
        Integer productId,
        Integer productCount,
        Double totalPrice,
        OrderStatus orderStatus) {
}