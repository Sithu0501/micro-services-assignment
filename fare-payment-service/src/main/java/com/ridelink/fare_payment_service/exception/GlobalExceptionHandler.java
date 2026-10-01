package com.ridelink.fare_payment_service.exception;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String message = "Validation failed";

        if (ex.getBindingResult().getFieldError() != null) {
            message = ex.getBindingResult()
                    .getFieldError()
                    .getDefaultMessage();
        }

        Map<String, Object> errorResponse = new HashMap<>();

        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "VALIDATION_ERROR");
        errorResponse.put("message", message);
        errorResponse.put("path", request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(DuplicateFinalFareException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateFinalFare(
            DuplicateFinalFareException ex,
            HttpServletRequest request) {

        Map<String, Object> errorResponse = new HashMap<>();

        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.CONFLICT.value());
        errorResponse.put("error", "DUPLICATE_FINAL_FARE");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("path", request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorResponse);
    }

    @ExceptionHandler(FareNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleFareNotFound(
            FareNotFoundException ex,
            HttpServletRequest request) {

        Map<String, Object> errorResponse = new HashMap<>();

        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.NOT_FOUND.value());
        errorResponse.put("error", "FARE_NOT_FOUND");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("path", request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePaymentNotFound(
            PaymentNotFoundException ex,
            HttpServletRequest request) {

        Map<String, Object> errorResponse = new HashMap<>();

        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.NOT_FOUND.value());
        errorResponse.put("error", "PAYMENT_NOT_FOUND");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("path", request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }

    @ExceptionHandler(DuplicatePaymentException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicatePayment(
            DuplicatePaymentException ex,
            HttpServletRequest request) {

        Map<String, Object> errorResponse = new HashMap<>();

        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.CONFLICT.value());
        errorResponse.put("error", "DUPLICATE_PAYMENT");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("path", request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorResponse);
    }

    @ExceptionHandler(ReceiptNotFoundException.class)
public ResponseEntity<Map<String, Object>> handleReceiptNotFound(
        ReceiptNotFoundException ex,
        HttpServletRequest request) {

    Map<String, Object> errorResponse = new HashMap<>();

    errorResponse.put("timestamp", LocalDateTime.now());
    errorResponse.put("status", HttpStatus.NOT_FOUND.value());
    errorResponse.put("error", "RECEIPT_NOT_FOUND");
    errorResponse.put("message", ex.getMessage());
    errorResponse.put("path", request.getRequestURI());

    return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(errorResponse);
    }
}