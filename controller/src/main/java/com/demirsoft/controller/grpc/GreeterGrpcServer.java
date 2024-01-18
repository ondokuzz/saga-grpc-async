package com.demirsoft.controller.grpc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.demirsoft.greeter.GreeterGrpc;
import com.demirsoft.greeter.Saga.HelloReply;
import com.demirsoft.greeter.Saga.HelloRequest;

import io.grpc.stub.StreamObserver;

public class GreeterGrpcServer extends GreeterGrpc.GreeterImplBase {
    private final Logger logger = LogManager.getLogger(getClass());

    @Override
    public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
        logger.debug("sayHello called with {}", req.getName());

        HelloReply reply = HelloReply.newBuilder().setMessage("Hello " + req.getName()).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}