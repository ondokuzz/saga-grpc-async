package com.example.inventory.services;

import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class InventoryServiceImpl implements InventoryService {
    private Integer totalProductCount = 100;

    public Boolean drop(Integer productId, Integer amount) {
        log.info("Will try to drop " + amount + " ...");
        totalProductCount -= amount;
        return true;
    }

    public Boolean rollback(Integer productId, Integer amount) {
        log.info("Will try to rollback drop by " + amount + " ...");
        totalProductCount += amount;
        return true;
    }

    public Boolean isProductAvailable(Integer productId, Integer amountt) {
        return totalProductCount > 0;
    }

}
