package com.demirsoft.micro1.payment.grpc.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.demirsoft.micro1.payment.grpc.GrpcPaymentServiceGrpc;
import com.demirsoft.micro1.payment.grpc.PaymentService.GrpcPaymentRequest;
import com.demirsoft.micro1.payment.grpc.PaymentService.GrpcPaymentResponse;
import com.demirsoft.micro1.payment.grpc.PaymentService.GrpcPaymentRollbackResponse;
import com.demirsoft.micro1.payment.grpc.PaymentService.GrpcPaymentRollbackStatus;
import com.demirsoft.micro1.payment.grpc.PaymentService.GrpcPaymentStatus;

import io.grpc.Context;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

public class PaymentServiceGrpcServer extends GrpcPaymentServiceGrpc.GrpcPaymentServiceImplBase {
    private final Logger logger = LogManager.getLogger(getClass());

    private Double balance = 100.0;

    @Override
    public void charge(GrpcPaymentRequest req, StreamObserver<GrpcPaymentResponse> responseObserver) {
        logger.debug("charge called with: {} balance is: {}", req, balance);

        checkIfPaymentRequestCancelled(responseObserver);

        GrpcPaymentResponse response = createPaymentResponse(req);

        logger.debug("charge returning: {} new balance: {}", response, balance);

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void rollback(GrpcPaymentRequest req, StreamObserver<GrpcPaymentRollbackResponse> responseObserver) {
        logger.debug("rollback called with: {} balance is: {}", req, balance);

        checkIfRollbackRequestCancelled(responseObserver);

        GrpcPaymentRollbackResponse response = createRollbackResponse(req);

        logger.debug("rollback returning: {} new balance is: {}", response, balance);

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private void checkIfPaymentRequestCancelled(StreamObserver<GrpcPaymentResponse> responseObserver) {
        if (Context.current().isCancelled())
            responseObserver.onError(
                    Status.CANCELLED.withDescription("Payment Request Cancelled by client").asRuntimeException());
    }

    private void checkIfRollbackRequestCancelled(StreamObserver<GrpcPaymentRollbackResponse> responseObserver) {
        if (Context.current().isCancelled())
            responseObserver.onError(
                    Status.CANCELLED.withDescription("Rollback Request Cancelled by client").asRuntimeException());
    }

    private GrpcPaymentResponse createPaymentResponse(GrpcPaymentRequest req) {
        GrpcPaymentStatus paymentStatus = this.buy(req.getPrice());

        return GrpcPaymentResponse.newBuilder()
                .setOrderId(req.getOrderId())
                .setUserId(req.getUserId())
                .setProductId(req.getProductId())
                .setProductCount(req.getProductCount())
                .setTotalPrice(req.getPrice())
                .setPaymentStatus(paymentStatus)
                .build();
    }

    private GrpcPaymentRollbackResponse createRollbackResponse(GrpcPaymentRequest req) {
        GrpcPaymentRollbackStatus rollbackStatus = this.refund(req.getPrice());

        GrpcPaymentRollbackResponse response = GrpcPaymentRollbackResponse.newBuilder()
                .setOrderId(req.getOrderId())
                .setUserId(req.getUserId())
                .setProductId(req.getProductId())
                .setProductCount(req.getProductCount())
                .setTotalPrice(req.getPrice())
                .setRollbackStatus(rollbackStatus)
                .build();
        return response;
    }

    private GrpcPaymentStatus buy(Double price) {
        if (price < this.balance) {
            balance -= price;
            return GrpcPaymentStatus.PAYMENT_COMPLETED;
        }

        return GrpcPaymentStatus.PAYMENT_CANCELLED;
    }

    private GrpcPaymentRollbackStatus refund(Double price) {
        if (price < 0)
            return GrpcPaymentRollbackStatus.ROLLBACK_FAILED;

        this.balance += price;
        return GrpcPaymentRollbackStatus.ROLLBACK_COMPLETED;

    }
}