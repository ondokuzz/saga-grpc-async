package com.demirsoft.apiservice.api.services.inventory;

import javax.annotation.Nonnull;

import reactor.core.publisher.Mono;

public interface InventoryService {
    Mono<InventoryResponse> drop(@Nonnull InventoryRequest inventoryRequest);

    Mono<InventoryResponse> rollback(@Nonnull InventoryRequest inventoryRequest);
}
