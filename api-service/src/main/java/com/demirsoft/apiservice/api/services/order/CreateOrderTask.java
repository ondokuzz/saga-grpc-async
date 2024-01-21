package com.demirsoft.apiservice.api.services.order;

import com.demirsoft.apiservice.api.saga.SagaTask;

import reactor.core.publisher.Mono;

public class CreateOrderTask implements SagaTask<OrderRequest> {

    private final OrderRequest orderInfo;

    public CreateOrderTask(OrderRequest orderInfo) {
        this.orderInfo = orderInfo;
    }

    @Override
    public Mono<OrderRequest> perform() {
        System.out.println("perform: order task");
        return Mono.just(this.orderInfo);
    }

    @Override
    public Mono<OrderRequest> rollback() {
        System.out.println("rollback: order task");
        return Mono.just(this.orderInfo);
    }

    @Override
    public void timeout() {
    }

}