package com.demirsoft.apiservice.api;

import java.time.LocalTime;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.demirsoft.apiservice.api.services.order.OrderRequest;
import com.demirsoft.apiservice.api.services.order.OrderResponse;
import com.demirsoft.apiservice.api.services.order.OrderService;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@RestController
@Log4j2
public class ApiController {

    private final OrderService orderService;

    ApiController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/orders")
    public Mono<OrderResponse> createOrder(@RequestBody OrderRequest orderRequest) {
        log.debug("creating order: {}", orderRequest);
        return orderService.createOrder(orderRequest);
    }

}
