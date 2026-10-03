package com.ridelink.fare_payment_service.exception;

public class RideNotFoundException extends RuntimeException {

    public RideNotFoundException(String message) {
        super(message);
    }
}
