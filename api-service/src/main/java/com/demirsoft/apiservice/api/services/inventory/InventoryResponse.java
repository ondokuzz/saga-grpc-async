package com.demirsoft.apiservice.api.services.inventory;

import com.demirsoft.apiservice.api.saga.TaskResponse;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@Getter
@RequiredArgsConstructor
final public class InventoryResponse implements TaskResponse {
    private final Integer orderId;
    private final Integer userId;
    private final Integer productId;
    private final Integer productCount;
    private final InventoryStatus inventoryStatus;
}
