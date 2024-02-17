package com.demirsoft.apiservice.api.saga;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

public class SagaTransaction {
    private static final int MAX_RETRY = 3;
    private final List<SagaTask<? extends TaskResponse>> tasks;

    public SagaTransaction(List<SagaTask<? extends TaskResponse>> tasks) {
        this.tasks = tasks;
    }

    private <T extends TaskResponse> Mono<T> taskPerformOrRollback(SagaTask<T> task) {
        return Mono.defer(() -> task.perform()
                .log()
                .retryWhen(Retry.backoff(MAX_RETRY, task.timeout()))
                .onErrorResume(ex -> taskRollback(task)));
    }

    private <T extends TaskResponse> Mono<T> taskRollback(SagaTask<T> task) {
        return task.rollback()
                .log()
                .retryWhen(Retry.backoff(MAX_RETRY, task.timeout()))
                .doOnError(ex -> System.out.println("log to dead letter queue: " + task + ": " + ex.getMessage()));
    }

    public Mono<TaskResponse[]> execute() {
        return Mono.zipDelayError(
                tasks.stream()
                        .peek(t -> System.out.println("stream task:" + t))
                        .map(task -> taskPerformOrRollback(task))
                        .peek(m -> System.out.println("stream mono:" + m))
                        .collect(Collectors.toList()),
                results -> {
                    return Arrays.asList(results).stream().toArray(TaskResponse[]::new);
                });
    }

}