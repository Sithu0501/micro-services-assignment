package com.ridelink.fare_payment_service.exception;

public class PaymentNotAllowedException extends RuntimeException {

    public PaymentNotAllowedException(String message) {
        super(message);
    }
}
