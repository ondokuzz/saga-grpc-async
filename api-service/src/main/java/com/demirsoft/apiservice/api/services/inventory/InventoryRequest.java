package com.demirsoft.apiservice.api.services.inventory;

public record InventoryRequest(Integer orderId, Integer userId, Integer productId, Integer productCount) {
}
