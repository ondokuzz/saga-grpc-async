package com.demirsoft.apiservice.api.services.inventory;

import org.springframework.lang.NonNull;

public enum InventoryStatus {
    AVAILABLE,
    UNAVAILABLE;

    private String errorMessage = "";

    public String getErrorMessage() {
        if (this.equals(AVAILABLE))
            throw new IllegalStateException();
        return this.errorMessage;
    }

    public InventoryStatus setErrorMessage(@NonNull String msg) {
        if (this.equals(AVAILABLE))
            throw new IllegalStateException();
        this.errorMessage = msg;
        return this;
    }

    @Override
    public String toString() {
        return this.equals(AVAILABLE) ? super.toString() : super.toString() + ": " + errorMessage;
    }
}