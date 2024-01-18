package com.demirsoft.apiservice.api.config;

import java.util.concurrent.ExecutionException;

import org.apache.kafka.common.errors.TimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ApiControllerExceptionHandler {

    @ExceptionHandler(InterruptedException.class)
    public ResponseEntity<String> handleInterruptedExceptions() {
        return new ResponseEntity<String>("request interrupted", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ExecutionException.class)
    public ResponseEntity<String> handleExecutionExceptions() {
        return new ResponseEntity<String>("request execution exception", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<String> handleTimeoutExceptions() {
        System.out.print("request timed out");
        return new ResponseEntity<String>("request timed out", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
