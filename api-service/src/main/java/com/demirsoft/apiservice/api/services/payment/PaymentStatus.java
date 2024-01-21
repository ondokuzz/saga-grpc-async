package com.demirsoft.apiservice.api.services.payment;

import org.springframework.lang.NonNull;

public enum PaymentStatus {
    PAYMENT_COMPLETED,
    PAYMENT_CANCELLED;

    private String errorMessage;

    public String getErrorMessage() {
        if (this.equals(PAYMENT_COMPLETED))
            throw new IllegalStateException();
        return this.errorMessage;
    }

    public PaymentStatus setErrorMessage(@NonNull String msg) {
        if (this.equals(PAYMENT_COMPLETED))
            throw new IllegalStateException();
        this.errorMessage = msg;
        return this;
    }

    @Override
    public String toString() {
        return this.equals(PAYMENT_COMPLETED) ? super.toString() : super.toString() + ": " + errorMessage;
    }
}