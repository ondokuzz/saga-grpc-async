package com.example.inventory.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.inventory.services.InventoryService;

@RestController
public class InventoryController {

    private InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/inventory/{productId}/availability")
    public Boolean getProductAvailability(@PathVariable Integer productId, @RequestParam("amount") Integer amount) {
        return inventoryService.isProductAvailable(productId, amount);
    }

    @PostMapping("/inventory/{productId}/drop")
    public Boolean dropProductByAmount(@PathVariable Integer productId, @RequestParam("amount") Integer amount) {
        return inventoryService.drop(productId, amount);
    }

    @PostMapping("/inventory/{productId}/rollback")
    public Boolean rollbackProductByAmount(@PathVariable Integer productId, @RequestParam("amount") Integer amount) {
        return inventoryService.rollback(productId, amount);
    }

}
