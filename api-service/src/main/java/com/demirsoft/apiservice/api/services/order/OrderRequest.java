package com.demirsoft.apiservice.api.services.order;

public record OrderRequest(Integer orderId, Integer userId, Integer productId, Integer productCount, Double price) {
}
