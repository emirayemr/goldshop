package com.emiray.goldshop.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    record ApiError(Instant timestamp, int status, String error, String message, String path, List<String> details) {}

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiError> onBind(BindException ex) {
        var details = ex.getAllErrors().stream().map(e -> e.getDefaultMessage()).toList();
        var body = new ApiError(Instant.now(), 400, "Bad Request", "Validation failed", "", details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> onGeneric(Exception ex) {
        var body = new ApiError(Instant.now(), 500, "Internal Server Error", ex.getMessage(), "", List.of());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
