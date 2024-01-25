package com.demirsoft.apiservice.api.grpc;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.demirsoft.apiservice.api.config.InventoryServiceProperties;
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
import com.google.common.util.concurrent.ListenableFuture;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@Log4j2
public class InventoryServiceGrpcClient implements InventoryService {
    private final GrpcInventoryServiceFutureStub grpcStub;
    private final InventoryServiceProperties inventoryServiceProperties;

    public InventoryServiceGrpcClient(
            final GrpcInventoryServiceFutureStub grpcStub,
            final InventoryServiceProperties inventoryServiceProperties) {

        this.grpcStub = grpcStub;
        this.inventoryServiceProperties = inventoryServiceProperties;
    }

    public Mono<InventoryResponse> drop(@Nonnull final InventoryRequest domainInventoryRequest) {
        log.info("Will try to drop " + domainInventoryRequest + " ...");

        GrpcInventoryRequest grpcInventoryRequest = mapDomainInventoryRequestToGrpc(domainInventoryRequest);

        ListenableFuture<GrpcInventoryResponse> grpcDropFuture;
        try {
            grpcDropFuture = callGrpcDrop(grpcInventoryRequest);
        } catch (Exception e) {
            log.debug("InventoryGrpc drop exception {} {}", e.getCause(), e.getMessage());
            return Mono.error(e);
        }

        return grpcDropResponseToMono(grpcDropFuture);
    }

    public Mono<InventoryResponse> rollback(@Nonnull final InventoryRequest domainInventoryRequest) {
        log.info("Will try to rollback " + domainInventoryRequest + " ...");

        GrpcInventoryRequest grpcInventoryRequest = mapDomainInventoryRequestToGrpc(domainInventoryRequest);

        ListenableFuture<GrpcInventoryRollbackResponse> grpcRollbackFuture;
        try {
            grpcRollbackFuture = callGrpcRollback(grpcInventoryRequest);
        } catch (Exception e) {
            log.debug("InventoryGrpc rollback exception {} {}", e.getCause(), e.getMessage());
            return Mono.error(e);
        }

        return grpcRollbackResponseToMono(grpcRollbackFuture);
    }

    private GrpcInventoryRequest mapDomainInventoryRequestToGrpc(
            final InventoryRequest domainInventoryRequest) {
        return GrpcInventoryRequest.newBuilder()
                .setOrderId(domainInventoryRequest.orderId())
                .setUserId(domainInventoryRequest.userId())
                .setProductId(domainInventoryRequest.productId())
                .setProductCount(domainInventoryRequest.productCount())
                .build();
    }

    private ListenableFuture<GrpcInventoryResponse> callGrpcDrop(final GrpcInventoryRequest grpcInventoryRequest) {
        return grpcStub
                .withDeadlineAfter(inventoryServiceProperties.getGrpcDeadline(), TimeUnit.SECONDS)
                .drop(grpcInventoryRequest);
    }

    private ListenableFuture<GrpcInventoryRollbackResponse> callGrpcRollback(
            final GrpcInventoryRequest grpcInventoryRequest) {
        return grpcStub
                .withDeadlineAfter(inventoryServiceProperties.getGrpcDeadline(), TimeUnit.SECONDS)
                .rollback(grpcInventoryRequest);
    }

    private Mono<InventoryResponse> grpcDropResponseToMono(
            @Nonnull final ListenableFuture<GrpcInventoryResponse> listenableFuture) {

        var completableFuture = ListenableToCompletable.convert(listenableFuture,
                (grpcDropResponse) -> new InventoryResponse(
                        grpcDropResponse.getOrderId(),
                        grpcDropResponse.getUserId(),
                        grpcDropResponse.getProductId(),
                        grpcDropResponse.getProductCount(),
                        mapGrpcInventoryStatusToDomain(grpcDropResponse.getInventoryStatus())));

        return Mono.fromFuture(completableFuture)
                .doOnError((ex) -> log.debug("grpcDropResponseToMono: consuming error {} {}", ex.getCause(),
                        ex.getMessage()))
                .onErrorComplete();
    }

    private Mono<InventoryResponse> grpcRollbackResponseToMono(
            @Nonnull final ListenableFuture<GrpcInventoryRollbackResponse> listenableFuture) {

        var completableFuture = ListenableToCompletable.convert(listenableFuture,
                (grpcRollbackResponse) -> new InventoryResponse(
                        grpcRollbackResponse.getOrderId(),
                        grpcRollbackResponse.getUserId(),
                        grpcRollbackResponse.getProductId(),
                        grpcRollbackResponse.getProductCount(),
                        mapGrpcInventoryRollbackStatusToDomain(grpcRollbackResponse.getRollbackStatus())));

        return Mono.fromFuture(completableFuture)
                .doOnError((ex) -> log.debug("grpcRollbackResponseToMono: consuming error {} {}",
                        ex.getCause(),
                        ex.getMessage()))
                .onErrorComplete();
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
                throw new IllegalArgumentException("Unknown Inventory Rollback Status: " + grpcInventoryRollbackStatus);
        }
    }

}
