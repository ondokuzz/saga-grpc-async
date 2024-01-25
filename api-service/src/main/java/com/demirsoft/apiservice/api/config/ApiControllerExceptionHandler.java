package com.demirsoft.apiservice.api.config;

import java.util.concurrent.ExecutionException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;

@RestControllerAdvice
public class ApiControllerExceptionHandler {

    @ExceptionHandler(InterruptedException.class)
    public ResponseEntity<String> handleInterruptedExceptions() {
        return new ResponseEntity<String>("request interrupted", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ExecutionException.class)
    public ResponseEntity<String> handleExecutionExceptions() {
        return new ResponseEntity<String>("request execution exception", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleStateExceptions(IllegalStateException ex) {
        return new ResponseEntity<String>("illegal state exception: " + ex.getCause() + " msg: " + ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(StatusRuntimeException.class)
    public ResponseEntity<String> handleStatusRuntimeExceptions(StatusRuntimeException ex) {
        return new ResponseEntity<String>(
                "grpc status runtime exception: " + ex.getCause() + " msg: " + ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(StatusException.class)
    public ResponseEntity<String> handleCompositeExceptions(StatusException ex) {
        return new ResponseEntity<String>("grpc status exception: " + ex.getCause() + " msg: " + ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleCompositeExceptions(Exception ex) {
        return new ResponseEntity<String>("grpc status exception: " + ex.getCause() + " msg: " + ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public ResponseEntity<String> handleGrpcExceptions(Exception ex) {
        return new ResponseEntity<String>("grpc status exception: " + ex.getCause() + " msg: " + ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
