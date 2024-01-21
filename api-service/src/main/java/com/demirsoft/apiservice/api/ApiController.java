package com.demirsoft.apiservice.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.demirsoft.apiservice.api.services.order.OrderRequest;
import com.demirsoft.apiservice.api.services.order.OrderResponse;
import com.demirsoft.apiservice.api.services.order.OrderService;

import reactor.core.publisher.Mono;

@RestController
public class ApiController {

    private final Logger logger = LogManager.getLogger(getClass());

    private final OrderService orderService;

    ApiController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/orders")
    public Mono<OrderResponse> createOrder(@RequestBody OrderRequest orderRequest) {
        logger.debug("creating order: {}", orderRequest);
        return orderService.createOrder(orderRequest);
    }

}
