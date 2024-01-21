package com.demirsoft.micro1.payment.grpc.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.demirsoft.micro1.payment.grpc.GrpcPaymentServiceGrpc;
import com.demirsoft.micro1.payment.grpc.PaymentService.GrpcPaymentRequest;
import com.demirsoft.micro1.payment.grpc.PaymentService.GrpcPaymentResponse;
import com.demirsoft.micro1.payment.grpc.PaymentService.GrpcPaymentRollbackResponse;
import com.demirsoft.micro1.payment.grpc.PaymentService.GrpcPaymentRollbackStatus;
import com.demirsoft.micro1.payment.grpc.PaymentService.GrpcPaymentStatus;

import io.grpc.stub.StreamObserver;

public class PaymentServiceGrpcServer extends GrpcPaymentServiceGrpc.GrpcPaymentServiceImplBase {
    private final Logger logger = LogManager.getLogger(getClass());

    private Double balance = 100.0;

    @Override
    public void charge(GrpcPaymentRequest req, StreamObserver<GrpcPaymentResponse> responseObserver) {
        logger.debug("charge called with {}", req);

        GrpcPaymentResponse response = GrpcPaymentResponse.newBuilder()
                .setOrderId(req.getOrderId())
                .setUserId(req.getUserId())
                .setProductId(req.getProductId())
                .setProductCount(req.getProductCount())
                .setTotalPrice(req.getPrice())
                .setPaymentStatus(this.buy(req.getPrice()))
                .build();

        logger.debug("charge returning {}", response);

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void rollback(GrpcPaymentRequest req, StreamObserver<GrpcPaymentRollbackResponse> responseObserver) {
        logger.debug("rollback called with {}", req);

        GrpcPaymentRollbackResponse response = GrpcPaymentRollbackResponse.newBuilder()
                .setOrderId(req.getOrderId())
                .setUserId(req.getUserId())
                .setProductId(req.getProductId())
                .setProductCount(req.getProductCount())
                .setTotalPrice(req.getPrice())
                .setRollbackStatus(this.add(req.getPrice()))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private GrpcPaymentStatus buy(Double price) {
        if (price < this.balance) {
            balance -= price;
            return GrpcPaymentStatus.PAYMENT_COMPLETED;
        }

        return GrpcPaymentStatus.PAYMENT_CANCELLED;
    }

    private GrpcPaymentRollbackStatus add(Double price) {
        this.balance += price;
        return GrpcPaymentRollbackStatus.ROLLBACK_COMPLETED;

    }
}