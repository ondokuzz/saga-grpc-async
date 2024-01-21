package com.demirsoft.apiservice.api.services.order;

import reactor.core.publisher.Mono;

public interface OrderService {
    Mono<OrderResponse> createOrder(OrderRequest orderInfo);
}
