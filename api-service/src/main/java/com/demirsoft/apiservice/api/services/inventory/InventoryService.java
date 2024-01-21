package com.demirsoft.apiservice.api.services.inventory;

import reactor.core.publisher.Mono;

public interface InventoryService {
    Mono<InventoryResponse> drop(InventoryRequest inventoryRequest);

    Mono<InventoryResponse> rollback(InventoryRequest inventoryRequest);
}
