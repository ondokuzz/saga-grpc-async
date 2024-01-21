package com.demirsoft.apiservice.api.grpc;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.demirsoft.apiservice.api.services.inventory.InventoryRequest;
import com.demirsoft.apiservice.api.services.inventory.InventoryResponse;
import com.demirsoft.apiservice.api.services.inventory.InventoryService;
import com.demirsoft.apiservice.api.services.inventory.InventoryStatus;
import com.demirsoft.micro1.payment.grpc.GrpcInventoryServiceGrpc.GrpcInventoryServiceFutureStub;
import com.demirsoft.micro1.payment.grpc.InventoryService.GrpcInventoryRequest;
import com.demirsoft.micro1.payment.grpc.InventoryService.GrpcInventoryResponse;
import com.demirsoft.micro1.payment.grpc.InventoryService.GrpcInventoryRollbackResponse;
import com.demirsoft.micro1.payment.grpc.InventoryService.GrpcInventoryRollbackStatus;
import com.demirsoft.micro1.payment.grpc.InventoryService.GrpcInventoryStatus;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import reactor.core.publisher.Mono;

public class InventoryServiceGrpcClient implements InventoryService {
    private final Logger logger = LogManager.getLogger(getClass());

    private GrpcInventoryServiceFutureStub grpcStub;

    public InventoryServiceGrpcClient(GrpcInventoryServiceFutureStub grpcStub) {
        this.grpcStub = grpcStub;
    }

    public CompletableFuture<InventoryResponse> toCompletableFuture(
            @Nonnull ListenableFuture<GrpcInventoryResponse> listenableFuture) {
        CompletableFuture<InventoryResponse> completableFuture = new CompletableFuture<InventoryResponse>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                boolean cancelled = listenableFuture.cancel(mayInterruptIfRunning);
                super.cancel(cancelled);
                return cancelled;
            }
        };

        Futures.addCallback(listenableFuture, new FutureCallback<GrpcInventoryResponse>() {
            @Override
            public void onSuccess(@Nonnull GrpcInventoryResponse result) {
                completableFuture
                        .complete(new InventoryResponse(
                                result.getOrderId(),
                                result.getUserId(),
                                result.getProductId(),
                                result.getProductCount(),
                                mapGrpcInventoryStatusToDomain(result.getInventoryStatus())));
            }

            @Override
            public void onFailure(@Nonnull Throwable ex) {
                completableFuture.completeExceptionally(ex);
            }
        }, MoreExecutors.directExecutor());

        return completableFuture;
    }

    public CompletableFuture<InventoryResponse> rollbackToCompletableFuture(
            @Nonnull ListenableFuture<GrpcInventoryRollbackResponse> listenableFuture) {
        CompletableFuture<InventoryResponse> completableFuture = new CompletableFuture<InventoryResponse>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                boolean cancelled = listenableFuture.cancel(mayInterruptIfRunning);
                super.cancel(cancelled);
                return cancelled;
            }
        };

        Futures.addCallback(listenableFuture, new FutureCallback<GrpcInventoryRollbackResponse>() {
            @Override
            public void onSuccess(@Nonnull GrpcInventoryRollbackResponse result) {
                completableFuture
                        .complete(new InventoryResponse(
                                result.getOrderId(),
                                result.getUserId(),
                                result.getProductId(),
                                result.getProductCount(),
                                mapGrpcInventoryRollbackStatusToDomain(result.getRollbackStatus())));
            }

            @Override
            public void onFailure(@Nonnull Throwable ex) {
                completableFuture.completeExceptionally(ex);
            }
        }, MoreExecutors.directExecutor());

        return completableFuture;
    }

    public Mono<InventoryResponse> drop(final InventoryRequest domainInventoryRequest) {
        logger.info("Will try to drop " + domainInventoryRequest + " ...");

        final GrpcInventoryRequest grpcInventoryRequest = mapDomainInventoryRequestToGrpc(domainInventoryRequest);

        ListenableFuture<GrpcInventoryResponse> grpcFuture = grpcStub
                .withDeadlineAfter(10, TimeUnit.SECONDS)
                .drop(grpcInventoryRequest);

        return Mono.fromFuture(toCompletableFuture(grpcFuture));
    }

    public Mono<InventoryResponse> rollback(final InventoryRequest domainInventoryRequest) {
        logger.info("Will try to rollback " + domainInventoryRequest + " ...");

        final GrpcInventoryRequest grpcInventoryRequest = mapDomainInventoryRequestToGrpc(domainInventoryRequest);

        ListenableFuture<GrpcInventoryRollbackResponse> grpcFuture = grpcStub
                .withDeadlineAfter(10, TimeUnit.SECONDS)
                .rollback(grpcInventoryRequest);

        return Mono.fromFuture(rollbackToCompletableFuture(grpcFuture));
    }

    private InventoryStatus mapGrpcInventoryStatusToDomain(final GrpcInventoryStatus grpcInventoryStatus) {
        switch (grpcInventoryStatus) {
            case AVAILABLE:
                return InventoryStatus.AVAILABLE;
            case UNAVAILABLE:
                return InventoryStatus.UNAVAILABLE;
            default:
                throw new IllegalArgumentException("Unknown Inventory Status: " + grpcInventoryStatus);
        }

    }

    private InventoryStatus mapGrpcInventoryRollbackStatusToDomain(
            final GrpcInventoryRollbackStatus grpcInventoryRollbackStatus) {
        switch (grpcInventoryRollbackStatus) {
            case INVENTORY_ROLLBACK_COMPLETED:
                return InventoryStatus.AVAILABLE;
            case INVENTORY_ROLLBACK_FAILED:
                return InventoryStatus.UNAVAILABLE;
            default:
                throw new IllegalArgumentException("Unknown Inventory Status: " + grpcInventoryRollbackStatus);
        }

    }

    private GrpcInventoryRequest mapDomainInventoryRequestToGrpc(final InventoryRequest domainInventoryRequest) {
        return GrpcInventoryRequest.newBuilder()
                .setOrderId(domainInventoryRequest.orderId())
                .setUserId(domainInventoryRequest.userId())
                .setProductId(domainInventoryRequest.productId())
                .setProductCount(domainInventoryRequest.productCount())
                .build();
    }
}
