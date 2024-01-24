package com.demirsoft.apiservice.api.services.inventory;

import java.time.Duration;

import javax.annotation.Nonnull;

import com.demirsoft.apiservice.api.saga.SagaTask;

import reactor.core.publisher.Mono;

public class InventoryTask implements SagaTask<InventoryResponse> {
    private static final Duration TIMEOUT = Duration.ofSeconds(1);

    private final InventoryService inventoryService;
    @Nonnull
    private final InventoryRequest inventoryRequest;

    public InventoryTask(InventoryService inventoryService, @Nonnull InventoryRequest inventoryRequest) {
        this.inventoryService = inventoryService;
        this.inventoryRequest = inventoryRequest;
    }

    @Override
    public Mono<InventoryResponse> perform() {
        System.out.println("perform: inventory task");
        return this.inventoryService.drop(inventoryRequest);
    }

    @Override
    public Mono<InventoryResponse> rollback() {
        System.out.println("rollback: inventory task");
        return this.inventoryService.rollback(inventoryRequest);

    }

    @Override
    public Duration timeout() {
        return InventoryTask.TIMEOUT;
    }

}