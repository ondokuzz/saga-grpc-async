package com.demirsoft.apiservice;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.demirsoft.apiservice.api.grpc.PaymentServiceGrpcClient;
import com.demirsoft.apiservice.api.saga.SagaTransaction;
import com.demirsoft.apiservice.api.services.inventory.InventoryRequest;
import com.demirsoft.apiservice.api.services.inventory.InventoryResponse;
import com.demirsoft.apiservice.api.services.inventory.InventoryService;
import com.demirsoft.apiservice.api.services.inventory.InventoryStatus;
import com.demirsoft.apiservice.api.services.inventory.InventoryTask;
import com.demirsoft.apiservice.api.services.order.OrderRequest;
import com.demirsoft.apiservice.api.services.order.OrderResponse;
import com.demirsoft.apiservice.api.services.order.OrderServiceImpl;
import com.demirsoft.apiservice.api.services.order.OrderStatus;
import com.demirsoft.apiservice.api.services.payment.PaymentRequest;
import com.demirsoft.apiservice.api.services.payment.PaymentResponse;
import com.demirsoft.apiservice.api.services.payment.PaymentStatus;
import com.demirsoft.apiservice.api.services.payment.PaymentTask;

import reactor.core.publisher.Mono;

public class SagaTransactionTest<T> {
    @Mock
    PaymentServiceGrpcClient paymentServiceGrpcClient;

    @Mock
    InventoryService inventoryService;

    @BeforeEach
    public void setup() {
        // if we don't call below, we will get NullPointerException
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createTaskTest() {
        OrderResponse orderResponse = new OrderResponse(1, 2, 3, 4, 50.0, new OrderStatus());
        Mono<PaymentResponse> m1 = Mono
                .just(new PaymentResponse(1, 2, 3, 4, 50.0, PaymentStatus.PAYMENT_CANCELLED.setErrorMessage("aaa")));
        Mono<InventoryResponse> m2 = Mono
                .just(new InventoryResponse(1, 2, 3, 4, InventoryStatus.UNAVAILABLE.setErrorMessage("bbb")));

        Mono.zip(m1, m2).map(tuple -> {

            OrderStatus combineOrderStatus = new OrderStatus().setPaymentStatus(tuple.getT1().paymentStatus())
                    .setInventoryStatus(tuple.getT2().inventoryStatus());

            return new OrderResponse(
                    orderResponse.orderId(),
                    orderResponse.userId(),
                    orderResponse.productId(),
                    orderResponse.productCount(),
                    orderResponse.totalPrice(),
                    combineOrderStatus);
        }).subscribe(response -> System.out.println(response));

    }

    @Test
    void createSagaTransactionTest() {
        when(inventoryService.isProductAvailable(eq(new InventoryRequest(1, 2, 3, 4))))
                .thenReturn(true);
        when(inventoryService.drop(eq(new InventoryRequest(1, 2, 3, 4))))
                .thenReturn(Mono.just(new InventoryResponse(1, 2, 3, 4, InventoryStatus.AVAILABLE)));

        when(paymentServiceGrpcClient.charge(eq(new PaymentRequest(1, 2, 3, 4, 5.0))))
                .thenReturn(Mono.just(new PaymentResponse(1, 2, 3, 4, 5.0, PaymentStatus.PAYMENT_COMPLETED)));

        var paymentRequest = new PaymentRequest(1, 2, 3, 4, 5.0);
        var inventoryRequest = new InventoryRequest(1, 2, 3, 4);

        var paymentTask = new PaymentTask(paymentServiceGrpcClient, paymentRequest);
        var inventoryTask = new InventoryTask(inventoryService, inventoryRequest);

        SagaTransaction transaction = new SagaTransaction(
                List.of(inventoryTask, paymentTask));

        transaction.execute().subscribe(taskResponses -> {
            System.out.println(taskResponses);
        });

        try {
            TimeUnit.SECONDS.sleep(15);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("test finished");

    }

    @Test
    void creatOrderTest() {
        var orderRequest = new OrderRequest(1, 2, 3, 4, 5.0);

        var orderService = new OrderServiceImpl(paymentServiceGrpcClient, inventoryService);

        when(inventoryService.isProductAvailable(eq(new InventoryRequest(1, 2, 3, 4))))
                .thenReturn(true);
        when(inventoryService.drop(eq(new InventoryRequest(1, 2, 3, 4))))
                .thenReturn(Mono.just(new InventoryResponse(1, 2, 3, 4, InventoryStatus.AVAILABLE)));

        when(paymentServiceGrpcClient.charge(eq(new PaymentRequest(1, 2, 3, 4, 5.0))))
                .thenReturn(Mono.just(new PaymentResponse(1, 2, 3, 4, 5.0, PaymentStatus.PAYMENT_COMPLETED)));

        orderService.createOrder(orderRequest).subscribe(System.out::println);
    }
}
