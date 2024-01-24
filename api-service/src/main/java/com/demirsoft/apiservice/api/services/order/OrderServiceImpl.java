package com.demirsoft.apiservice.api.services.order;

import java.util.Arrays;
import java.util.List;

import com.demirsoft.apiservice.api.saga.SagaTransaction;
import com.demirsoft.apiservice.api.saga.TaskResponse;
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

    private final PaymentService paymentService;
    private final InventoryService inventoryService;

    public OrderServiceImpl(PaymentService paymentService, InventoryService inventoryService) {
        this.paymentService = paymentService;
        this.inventoryService = inventoryService;
    }

    @Override
    public Mono<OrderResponse> createOrder(OrderRequest orderRequest) {

        var paymentRequest = createPaymentRequest(orderRequest);
        var paymentTask = createPaymentTask(paymentRequest);

        var inventoryRequest = createInventoryRequest(orderRequest);
        var inventoryTask = createInventoryTask(inventoryRequest);

        Mono<TaskResponse[]> taskResponses = executeTasks(paymentTask, inventoryTask);

        return createCombinedResponse(orderRequest, taskResponses);

    }

    private Mono<TaskResponse[]> executeTasks(PaymentTask paymentTask, InventoryTask inventoryTask) {
        SagaTransaction transaction = createTransaction(paymentTask, inventoryTask);

        return transaction.execute();
    }

    private SagaTransaction createTransaction(PaymentTask paymentTask, InventoryTask inventoryTask) {
        return new SagaTransaction(
                List.of(paymentTask, inventoryTask));
    }

    private InventoryTask createInventoryTask(InventoryRequest inventoryRequest) {
        return new InventoryTask(this.inventoryService, inventoryRequest);
    }

    private PaymentTask createPaymentTask(PaymentRequest paymentRequest) {
        return new PaymentTask(this.paymentService, paymentRequest);
    }

    private InventoryRequest createInventoryRequest(OrderRequest orderRequest) {
        return new InventoryRequest(
                orderRequest.orderId(), orderRequest.userId(),
                orderRequest.productId(), orderRequest.productCount());
    }

    private PaymentRequest createPaymentRequest(OrderRequest orderRequest) {
        return new PaymentRequest(
                orderRequest.orderId(), orderRequest.userId(),
                orderRequest.productId(), orderRequest.productCount(), orderRequest.price());
    }

    private OrderResponse createOrderResponse(OrderRequest orderRequest, Object[] taskResponses) {

        OrderStatus orderStatus = new OrderStatus();
        AtomicDouble totalPrice = new AtomicDouble(0.0);

        extractInfoFromTaskResponses(taskResponses, orderStatus, totalPrice);

        return new OrderResponse(
                orderRequest.orderId(),
                orderRequest.userId(),
                orderRequest.productId(),
                orderRequest.productCount(),
                totalPrice.get(),
                orderStatus);
    }

    private void extractInfoFromTaskResponses(
            Object[] taskResponses,
            OrderStatus orderStatus,
            AtomicDouble totalPrice) {
        Arrays.asList(taskResponses).stream().forEach(taskResponse -> {
            if (taskResponse instanceof PaymentResponse) {
                processPaymentResponse((PaymentResponse) taskResponse, orderStatus, totalPrice);
            } else if (taskResponse instanceof InventoryResponse) {
                processInventoryResponse((InventoryResponse) taskResponse, orderStatus);
            } else {
                throw new IllegalArgumentException("Unexpected Task Response Type");
            }
        });
    }

    private void processInventoryResponse(InventoryResponse inventoryResponse, OrderStatus orderStatus) {
        orderStatus.setInventoryStatus(inventoryResponse.inventoryStatus());
    }

    private void processPaymentResponse(PaymentResponse paymentResponse, OrderStatus orderStatus,
            AtomicDouble totalPrice) {
        orderStatus.setPaymentStatus(paymentResponse.paymentStatus());
        totalPrice.set(paymentResponse.totalPrice());
    }

    private Mono<OrderResponse> createCombinedResponse(OrderRequest orderRequest, Mono<TaskResponse[]> taskResponses) {
        return taskResponses.map(taskResponseArray -> {
            return createOrderResponse(orderRequest, taskResponseArray);
        });
    }

}
