package com.demirsoft.apiservice.api.services.order;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.demirsoft.apiservice.api.saga.SagaTransaction;
import com.demirsoft.apiservice.api.services.inventory.InventoryRequest;
import com.demirsoft.apiservice.api.services.inventory.InventoryResponse;
import com.demirsoft.apiservice.api.services.inventory.InventoryService;
import com.demirsoft.apiservice.api.services.inventory.InventoryTask;
import com.demirsoft.apiservice.api.services.payment.PaymentRequest;
import com.demirsoft.apiservice.api.services.payment.PaymentResponse;
import com.demirsoft.apiservice.api.services.payment.PaymentService;
import com.demirsoft.apiservice.api.services.payment.PaymentTask;
import com.google.common.util.concurrent.AtomicDouble;

import reactor.core.publisher.Mono;

public class OrderServiceImpl implements OrderService {

    private final Logger logger = LogManager.getLogger(getClass());

    private final PaymentService paymentService;
    private final InventoryService inventoryService;

    public OrderServiceImpl(PaymentService paymentService, InventoryService inventoryService) {
        this.paymentService = paymentService;
        this.inventoryService = inventoryService;
    }

    @Override
    public Mono<OrderResponse> createOrder(OrderRequest orderRequest) {
        PaymentRequest paymentRequest = new PaymentRequest(
                orderRequest.orderId(), orderRequest.userId(),
                orderRequest.productId(), orderRequest.productCount(), orderRequest.price());

        InventoryRequest inventoryRequest = new InventoryRequest(
                orderRequest.orderId(), orderRequest.userId(),
                orderRequest.productId(), orderRequest.productCount());

        var paymentTask = new PaymentTask(this.paymentService, paymentRequest);
        var inventoryTask = new InventoryTask(this.inventoryService, inventoryRequest);

        SagaTransaction transaction = new SagaTransaction(
                List.of(paymentTask),
                Duration.ofSeconds(10));

        return toOrderResponse(orderRequest, transaction.execute());

    }

    private OrderResponse createOrderResponse(OrderRequest orderRequest, Object[] taskResponses) {

        OrderStatus orderStatus = new OrderStatus();
        AtomicDouble totalPrice = new AtomicDouble(0.0);

        Arrays.asList(taskResponses).stream().forEach(r -> {
            if (r instanceof PaymentResponse) {
                PaymentResponse paymentResponse = (PaymentResponse) r;
                orderStatus.setPaymentStatus(paymentResponse.paymentStatus());
                totalPrice.set(paymentResponse.totalPrice());
            } else if (r instanceof InventoryResponse) {
                InventoryResponse inventoryResponse = (InventoryResponse) r;
                orderStatus.setInventoryStatus(inventoryResponse.inventoryStatus());
            } else {
                throw new IllegalArgumentException("Unexpected Task Response Type");
            }
        });

        return new OrderResponse(
                orderRequest.orderId(),
                orderRequest.userId(),
                orderRequest.productId(),
                orderRequest.productCount(),
                totalPrice.get(),
                orderStatus);

    }

    private Mono<OrderResponse> toOrderResponse(OrderRequest orderRequest, Mono<Object[]> taskResponses) {
        return taskResponses.map(taskResponseArray -> {
            return createOrderResponse(orderRequest, taskResponseArray);
        });
    }

}
