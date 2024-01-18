package com.demirsoft.apiservice.api.grpc;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.demirsoft.greeter.GreeterGrpc.GreeterStub;
import com.demirsoft.greeter.Saga.HelloReply;
import com.demirsoft.greeter.Saga.HelloRequest;

import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

public class GreeterGrpcClient {
    private final Logger logger = LogManager.getLogger(getClass());

    private GreeterStub asyncGrpcStub;

    public GreeterGrpcClient(GreeterStub asyncGrpcStub) {
        this.asyncGrpcStub = asyncGrpcStub;
    }

    private StreamObserver<HelloReply> processHelloResponseAsync() {
        return new StreamObserver<HelloReply>() {

            @Override
            public void onNext(HelloReply response) {
                logger.info("Greeting received from server: " + response.getMessage());
                // received.add(new Site(response.getMessage(),
                // Date.from(ZonedDateTime.now().toInstant())));
            }

            @Override
            public void onError(Throwable t) {
                logger.info("Greeting received error from server {0}", t.getMessage());
            }

            @Override
            public synchronized void onCompleted() {
                logger.info("Greeting stream completed");
                this.notify();
            }

        };
    }

    public StreamObserver<HelloReply> greet(final String name) {
        logger.info("Will try to greet " + name + " ...");

        final HelloRequest request = HelloRequest.newBuilder().setName(name).build();
        StreamObserver<HelloReply> processHelloResponseAsync = processHelloResponseAsync();
        try {
            logger.info("making the call for " + name + " ...");
            asyncGrpcStub.withDeadlineAfter(10, TimeUnit.SECONDS).sayHello(request, processHelloResponseAsync);
            logger.info("call returned for " + name + " ...");
        } catch (final StatusRuntimeException e) {
            e.printStackTrace();
            logger.info("async RPC failed: {0}", e.getStatus());
        }

        return processHelloResponseAsync;
    }
}
