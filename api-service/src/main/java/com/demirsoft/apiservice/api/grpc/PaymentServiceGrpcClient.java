package com.demirsoft.apiservice.api.grpc;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.demirsoft.apiservice.api.services.payment.PaymentRequest;
import com.demirsoft.apiservice.api.services.payment.PaymentResponse;
import com.demirsoft.apiservice.api.services.payment.PaymentService;
import com.demirsoft.apiservice.api.services.payment.PaymentStatus;
import com.demirsoft.micro1.payment.grpc.GrpcPaymentServiceGrpc.GrpcPaymentServiceFutureStub;
import com.demirsoft.micro1.payment.grpc.PaymentService.GrpcPaymentRequest;
import com.demirsoft.micro1.payment.grpc.PaymentService.GrpcPaymentResponse;
import com.demirsoft.micro1.payment.grpc.PaymentService.GrpcPaymentRollbackResponse;
import com.demirsoft.micro1.payment.grpc.PaymentService.GrpcPaymentRollbackStatus;
import com.demirsoft.micro1.payment.grpc.PaymentService.GrpcPaymentStatus;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import reactor.core.publisher.Mono;

public class PaymentServiceGrpcClient implements PaymentService {
    private final Logger logger = LogManager.getLogger(getClass());

    private GrpcPaymentServiceFutureStub grpcStub;

    public PaymentServiceGrpcClient(GrpcPaymentServiceFutureStub grpcStub) {
        this.grpcStub = grpcStub;
    }

    public CompletableFuture<PaymentResponse> toCompletableFuture(
            @Nonnull ListenableFuture<GrpcPaymentResponse> listenableFuture) {

        CompletableFuture<PaymentResponse> completableFuture = new CompletableFuture<PaymentResponse>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                boolean cancelled = listenableFuture.cancel(mayInterruptIfRunning);
                super.cancel(cancelled);
                return cancelled;
            }
        };

        Futures.addCallback(listenableFuture, new FutureCallback<GrpcPaymentResponse>() {
            @Override
            public void onSuccess(@Nonnull GrpcPaymentResponse result) {
                completableFuture
                        .complete(new PaymentResponse(
                                result.getOrderId(),
                                result.getUserId(),
                                result.getProductId(),
                                result.getProductCount(),
                                result.getTotalPrice(),
                                mapGrpcPaymentStatusToDomain(result.getPaymentStatus())));
            }

            @Override
            public void onFailure(@Nonnull Throwable ex) {
                completableFuture.completeExceptionally(ex);
            }
        }, MoreExecutors.directExecutor());

        return completableFuture;
    }

    public CompletableFuture<PaymentResponse> rollbackToCompletableFuture(
            @Nonnull ListenableFuture<GrpcPaymentRollbackResponse> listenableFuture) {

        CompletableFuture<PaymentResponse> completableFuture = new CompletableFuture<PaymentResponse>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                boolean cancelled = listenableFuture.cancel(mayInterruptIfRunning);
                super.cancel(cancelled);
                return cancelled;
            }
        };

        Futures.addCallback(listenableFuture, new FutureCallback<GrpcPaymentRollbackResponse>() {
            @Override
            public void onSuccess(@Nonnull GrpcPaymentRollbackResponse result) {
                completableFuture
                        .complete(new PaymentResponse(
                                result.getOrderId(),
                                result.getUserId(),
                                result.getProductId(),
                                result.getProductCount(),
                                result.getTotalPrice(),
                                mapGrpcPaymentRollbackStatusToDomain(result.getRollbackStatus())));
            }

            @Override
            public void onFailure(@Nonnull Throwable ex) {
                completableFuture.completeExceptionally(ex);
            }
        }, MoreExecutors.directExecutor());

        return completableFuture;
    }

    public Mono<PaymentResponse> charge(final PaymentRequest domainPaymentRequest) {
        logger.info("Will try to charge " + domainPaymentRequest + " ...");

        final GrpcPaymentRequest grpcPaymentRequest = mapDomainPaymentRequestToGrpc(domainPaymentRequest);

        ListenableFuture<GrpcPaymentResponse> grpcFuture = grpcStub
                .withDeadlineAfter(10, TimeUnit.SECONDS)
                .charge(grpcPaymentRequest);

        return Mono.fromFuture(toCompletableFuture(grpcFuture));
    }

    public Mono<PaymentResponse> rollback(final PaymentRequest domainPaymentRequest) {
        logger.info("Will try to rollback " + domainPaymentRequest + " ...");

        final GrpcPaymentRequest grpcPaymentRequest = mapDomainPaymentRequestToGrpc(domainPaymentRequest);

        ListenableFuture<GrpcPaymentRollbackResponse> grpcFuture = grpcStub
                .withDeadlineAfter(10, TimeUnit.SECONDS)
                .rollback(grpcPaymentRequest);

        return Mono.fromFuture(rollbackToCompletableFuture(grpcFuture));
    }

    private PaymentStatus mapGrpcPaymentStatusToDomain(final GrpcPaymentStatus grpcPaymentStatus) {
        switch (grpcPaymentStatus) {
            case PAYMENT_COMPLETED:
                return PaymentStatus.PAYMENT_COMPLETED;
            case PAYMENT_CANCELLED:
                return PaymentStatus.PAYMENT_CANCELLED;
            default:
                throw new IllegalArgumentException("Unknown Payment Status: " + grpcPaymentStatus);
        }

    }

    private PaymentStatus mapGrpcPaymentRollbackStatusToDomain(
            final GrpcPaymentRollbackStatus grpcPaymentRollbackStatus) {
        switch (grpcPaymentRollbackStatus) {
            case PAYMENT_ROLLBACK_COMPLETED:
                return PaymentStatus.PAYMENT_COMPLETED;
            case PAYMENT_ROLLBACK_FAILED:
                return PaymentStatus.PAYMENT_CANCELLED;
            default:
                throw new IllegalArgumentException("Unknown Payment Status: " + grpcPaymentRollbackStatus);
        }

    }

    private GrpcPaymentRequest mapDomainPaymentRequestToGrpc(final PaymentRequest domainPaymentRequest) {
        return GrpcPaymentRequest.newBuilder()
                .setOrderId(domainPaymentRequest.orderId())
                .setUserId(domainPaymentRequest.userId())
                .setProductId(domainPaymentRequest.productId())
                .setProductCount(domainPaymentRequest.productCount())
                .setPrice(domainPaymentRequest.price())
                .build();
    }
}
