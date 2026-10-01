package com.ridelink.fare_payment_service.exception;

public class ReceiptNotFoundException extends RuntimeException {

    public ReceiptNotFoundException(String message) {
        super(message);
    }
}