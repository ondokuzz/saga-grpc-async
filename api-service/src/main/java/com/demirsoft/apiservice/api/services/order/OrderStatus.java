package com.demirsoft.apiservice.api.services.order;

import com.demirsoft.apiservice.api.services.inventory.InventoryStatus;
import com.demirsoft.apiservice.api.services.payment.PaymentStatus;

public class OrderStatus {
    public enum OrderStatusCode {
        ORDER_COMPLETED,
        ORDER_CANCELLED
    }

    OrderStatusCode orderStatusCode = OrderStatusCode.ORDER_COMPLETED;

    PaymentStatus paymentStatus = PaymentStatus.PAYMENT_COMPLETED;
    InventoryStatus inventoryStatus = InventoryStatus.AVAILABLE;

    public OrderStatus setInventoryStatus(InventoryStatus inventoryStatus) {
        this.inventoryStatus = inventoryStatus;

        return this.reevaluateStatus();

    }

    public OrderStatus setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;

        return this.reevaluateStatus();
    }

    @Override
    public String toString() {
        return this.orderStatusCode.toString() +
                ": payment status:" + paymentStatus.toString() +
                ", inventory status:" + inventoryStatus.toString();

    }

    private OrderStatus reevaluateStatus() {
        if (this.inventoryStatus.equals(InventoryStatus.AVAILABLE) &&
                this.paymentStatus.equals(PaymentStatus.PAYMENT_COMPLETED))
            this.orderStatusCode = OrderStatusCode.ORDER_COMPLETED;
        else
            this.orderStatusCode = OrderStatusCode.ORDER_CANCELLED;

        return this;
    }
}
