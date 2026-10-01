package com.ridelink.fare_payment_service.exception;

public class DuplicateFinalFareException extends RuntimeException {

    public DuplicateFinalFareException(String message) {
        super(message);
    }
}