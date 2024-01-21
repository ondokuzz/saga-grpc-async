package com.demirsoft.apiservice.api.saga;

import reactor.core.publisher.Mono;

public interface SagaTask<Response> {
    Mono<Response> perform();

    Mono<Response> rollback();

    void timeout();
}
