package com.demirsoft.apiservice.api.services.order;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import com.demirsoft.apiservice.api.saga.SagaTask;
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

        var inventoryRequest = createInventoryRequest(orderRequest);
        if (!productAvailable(inventoryRequest))
            return productUnavailableError(inventoryRequest);

        var paymentRequest = createPaymentRequest(orderRequest);
        var paymentTask = createPaymentTask(paymentRequest);

        var inventoryTask = createInventoryTask(inventoryRequest);

        var taskList = List.<SagaTask<? extends TaskResponse>>of(paymentTask, inventoryTask);
        var taskResponses = executeTasks(taskList);

        return createCombinedResponse(orderRequest, taskResponses);

    }

    private Mono<OrderResponse> productUnavailableError(InventoryRequest inventoryRequest) {
        return Mono.error(
                new IllegalArgumentException(
                        String.format("product: %d for amount: %d not available",
                                inventoryRequest.productId(),
                                inventoryRequest.productCount())));
    }

    private Mono<TaskResponse[]> executeTasks(List<SagaTask<? extends TaskResponse>> taskList) {
        SagaTransaction transaction = new SagaTransaction(taskList);

        return transaction.execute();
    }

    private InventoryTask createInventoryTask(@Nonnull InventoryRequest inventoryRequest) {
        return new InventoryTask(this.inventoryService, inventoryRequest);
    }

    private Boolean productAvailable(@Nonnull InventoryRequest inventoryRequest) {
        return inventoryService.isProductAvailable(inventoryRequest);
    }

    private PaymentTask createPaymentTask(PaymentRequest paymentRequest) {
        return new PaymentTask(this.paymentService, paymentRequest);
    }

    @Nonnull
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
