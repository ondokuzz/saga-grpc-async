package com.demirsoft.apiservice.api.saga;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

public class SagaTransaction {
    private final List<SagaTask<?>> tasks;
    private final Duration timeout;

    public SagaTransaction(List<SagaTask<?>> tasks, Duration timeout) {
        this.tasks = tasks;
        this.timeout = timeout;
    }

    private <T> Mono<T> performOrRollbackTask(SagaTask<T> task) {
        return Mono.defer(() -> task.perform()
                .log()
                .retryWhen(Retry.backoff(3, timeout))
                .onErrorResume(ex -> rollbackTask(task)));
    }

    private <T> Mono<T> rollbackTask(SagaTask<T> task) {
        return task.rollback()
                .log()
                .retryWhen(Retry.backoff(3, timeout))
                .doOnError(ex -> System.out.println("log to dead letter queue: " + task + ": " + ex.getMessage()));
    }

    public Mono<Object[]> execute() {
        return Mono.zipDelayError(
                tasks.stream()
                        .peek(t -> System.out.println("stream task:" + t))
                        .map(task -> performOrRollbackTask(task))
                        .peek(m -> System.out.println("stream mono:" + m))
                        .collect(Collectors.toList()),
                results -> {
                    return results;
                });
    }

}