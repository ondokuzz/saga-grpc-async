package com.demirsoft.apiservice.api.services;

public class InventoryServiceUnreachableException extends RuntimeException {
    private final String message;

    public InventoryServiceUnreachableException(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "InventoryServiceUnreachableException [message=" + message + "]";
    }

}
