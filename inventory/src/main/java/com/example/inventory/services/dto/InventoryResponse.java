package com.example.inventory.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
@Getter
@RequiredArgsConstructor
final public class InventoryResponse {
    private final Integer orderId;
    private final Integer userId;
    private final Integer productId;
    private final Integer productCount;
    private final InventoryStatus inventoryStatus;
}
