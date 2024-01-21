package com.demirsoft.apiservice.api.services.inventory;

public record InventoryResponse(
                Integer orderId,
                Integer userId,
                Integer productId,
                Integer productCount,
                InventoryStatus inventoryStatus) {
}
