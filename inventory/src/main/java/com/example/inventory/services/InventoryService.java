package com.example.inventory.services;

public interface InventoryService {
    Boolean drop(Integer productId, Integer amount);

    Boolean rollback(Integer productId, Integer amount);

    Boolean isProductAvailable(Integer productId, Integer amount);
}
