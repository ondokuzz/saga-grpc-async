package com.demirsoft.apiservice.api.grpc;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import lombok.extern.log4j.Log4j2;

import javax.annotation.Nonnull;

@Log4j2
public class ListenableToCompletable {

    public static <G, D> CompletableFuture<D> convert(
            @Nonnull ListenableFuture<G> grpcListenableFuture,
            @Nonnull Function<G, D> grpcToDomainConverter) {

        CompletableFuture<D> completableFuture = createCompletableFromListenable(grpcListenableFuture);

        attachListenableCallbacksToCompletable(completableFuture, grpcListenableFuture, grpcToDomainConverter);

        return completableFuture;
    }

    @Nonnull
    private static <D, G> CompletableFuture<D> createCompletableFromListenable(
            ListenableFuture<G> listenableFuture) {
        return new CompletableFuture<D>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                boolean cancelled = listenableFuture.cancel(mayInterruptIfRunning);
                super.cancel(cancelled);
                return cancelled;
            }
        };
    }

    private static <G, D> void attachListenableCallbacksToCompletable(
            @Nonnull CompletableFuture<D> completableFuture,
            @Nonnull ListenableFuture<G> grpcListenableFuture,
            Function<G, D> grpcToDomainConverter) {

        Futures.addCallback(
                grpcListenableFuture,
                createCallbackForCompletable(completableFuture, grpcToDomainConverter),
                MoreExecutors.directExecutor());
    }

    @Nonnull
    private static <G, D> FutureCallback<G> createCallbackForCompletable(
            @Nonnull CompletableFuture<D> completableFuture,
            @Nonnull Function<G, D> grpcToDomainConverter) {

        return new FutureCallback<G>() {
            @Override
            public void onSuccess(@Nonnull G result) {
                completableFuture.complete(grpcToDomainConverter.apply(result));
            }

            @Override
            public void onFailure(@Nonnull Throwable ex) {
                log.error("grpc exception occured: {} {} {}", ex.getCause(), ex.getMessage(), ex.toString());
                // completableFuture.completeExceptionally(ex);
            }
        };
    }

}
