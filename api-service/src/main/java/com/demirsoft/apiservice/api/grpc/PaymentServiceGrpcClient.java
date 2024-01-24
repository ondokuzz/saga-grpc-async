package com.demirsoft.apiservice.api.grpc;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.demirsoft.apiservice.api.config.PaymentServiceProperties;
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

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@Log4j2
public class PaymentServiceGrpcClient implements PaymentService {

    private final GrpcPaymentServiceFutureStub grpcStub;
    private final PaymentServiceProperties paymentServiceProperties;

    public PaymentServiceGrpcClient(GrpcPaymentServiceFutureStub grpcStub,
            PaymentServiceProperties paymentServiceProperties) {
        this.grpcStub = grpcStub;
        this.paymentServiceProperties = paymentServiceProperties;
    }

    public Mono<PaymentResponse> charge(final PaymentRequest domainPaymentRequest) {
        log.info("Will try to charge " + domainPaymentRequest + " ...");

        final GrpcPaymentRequest grpcPaymentRequest = mapDomainPaymentRequestToGrpc(domainPaymentRequest);

        ListenableFuture<GrpcPaymentResponse> grpcChargeFuture = grpcCharge(grpcPaymentRequest);

        return grpcChargeResponseToMono(grpcChargeFuture);
    }

    public Mono<PaymentResponse> rollback(final PaymentRequest domainPaymentRequest) {
        log.info("Will try to rollback " + domainPaymentRequest + " ...");

        final GrpcPaymentRequest grpcPaymentRequest = mapDomainPaymentRequestToGrpc(domainPaymentRequest);

        ListenableFuture<GrpcPaymentRollbackResponse> grpcRollbackFuture = grpcRollback(grpcPaymentRequest);

        return grpcRollbackResponseToMono(grpcRollbackFuture);
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

    private ListenableFuture<GrpcPaymentResponse> grpcCharge(final GrpcPaymentRequest grpcPaymentRequest) {
        return grpcStub
                .withDeadlineAfter(paymentServiceProperties.getGrpcDeadline(), TimeUnit.SECONDS)
                .charge(grpcPaymentRequest);
    }

    private ListenableFuture<GrpcPaymentRollbackResponse> grpcRollback(final GrpcPaymentRequest grpcPaymentRequest) {
        return grpcStub
                .withDeadlineAfter(paymentServiceProperties.getGrpcDeadline(), TimeUnit.SECONDS)
                .rollback(grpcPaymentRequest);
    }

    private Mono<PaymentResponse> grpcChargeResponseToMono(
            @Nonnull final ListenableFuture<GrpcPaymentResponse> listenableFuture) {

        var completableFuture = ListenableToCompletable.convert(listenableFuture,
                (grpcChargeResponse) -> new PaymentResponse(
                        grpcChargeResponse.getOrderId(),
                        grpcChargeResponse.getUserId(),
                        grpcChargeResponse.getProductId(),
                        grpcChargeResponse.getProductCount(),
                        grpcChargeResponse.getTotalPrice(),
                        mapGrpcPaymentStatusToDomain(grpcChargeResponse.getPaymentStatus())));

        return Mono.fromFuture(completableFuture);
    }

    private Mono<PaymentResponse> grpcRollbackResponseToMono(
            @Nonnull final ListenableFuture<GrpcPaymentRollbackResponse> listenableFuture) {

        var completableFuture = ListenableToCompletable.convert(listenableFuture,
                (grpcRollbackResponse) -> new PaymentResponse(
                        grpcRollbackResponse.getOrderId(),
                        grpcRollbackResponse.getUserId(),
                        grpcRollbackResponse.getProductId(),
                        grpcRollbackResponse.getProductCount(),
                        grpcRollbackResponse.getTotalPrice(),
                        mapGrpcPaymentRollbackStatusToDomain(grpcRollbackResponse.getRollbackStatus())));

        return Mono.fromFuture(completableFuture);
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
}
