package com.demirsoft.apiservice.api.config;

import io.grpc.Status;
import io.grpc.StatusException;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GrpcAdviceConfiguration {

    public StatusException handleGrpcExceptions(Exception ex) {
        log.error("grpc exception. cause: {} message: {}", ex.getCause(), ex.getMessage());

        return Status.INTERNAL.withDescription("grpc exception to status exception").asException();
    }

}
