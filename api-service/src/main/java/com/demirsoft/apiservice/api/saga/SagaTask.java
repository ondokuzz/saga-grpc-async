package com.demirsoft.apiservice.api.saga;

import java.time.Duration;

import reactor.core.publisher.Mono;

public interface SagaTask<Response> {
    Mono<Response> perform();

    Mono<Response> rollback();

    Duration timeout();
}
