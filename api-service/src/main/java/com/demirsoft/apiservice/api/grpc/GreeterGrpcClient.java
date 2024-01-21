package com.demirsoft.apiservice.api.grpc;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.demirsoft.apiservice.api.model.Site;
import com.demirsoft.greeter.GreeterGrpc.GreeterFutureStub;
import com.demirsoft.greeter.Saga.HelloReply;
import com.demirsoft.greeter.Saga.HelloRequest;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import reactor.core.publisher.Mono;

public class GreeterGrpcClient {
    private final Logger logger = LogManager.getLogger(getClass());

    private GreeterFutureStub grpcStub;

    public GreeterGrpcClient(GreeterFutureStub grpcStub) {
        this.grpcStub = grpcStub;
    }

    public CompletableFuture<Site> toCompletableFuture(@Nonnull ListenableFuture<HelloReply> listenableFuture) {
        CompletableFuture<Site> completableFuture = new CompletableFuture<Site>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                boolean cancelled = listenableFuture.cancel(mayInterruptIfRunning);
                super.cancel(cancelled);
                return cancelled;
            }
        };

        Futures.addCallback(listenableFuture, new FutureCallback<HelloReply>() {
            @Override
            public void onSuccess(@Nonnull HelloReply result) {
                completableFuture
                        .complete(new Site("demirsoy", Date.from(ZonedDateTime.now().plusMonths(1).toInstant())));
            }

            @Override
            public void onFailure(@Nonnull Throwable ex) {
                completableFuture.completeExceptionally(ex);
            }
        }, MoreExecutors.directExecutor());

        return completableFuture;
    }

    public Mono<Site> greet(final String name) {
        logger.info("Will try to greet " + name + " ...");

        final HelloRequest request = HelloRequest.newBuilder().setName(name).build();
        ListenableFuture<HelloReply> future = grpcStub.withDeadlineAfter(10, TimeUnit.SECONDS).sayHello(request);
        logger.info("call returned for " + name + " ...");
        return Mono.fromFuture(toCompletableFuture(future));
    }
}
