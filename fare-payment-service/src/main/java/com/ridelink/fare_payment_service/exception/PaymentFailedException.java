package com.ridelink.fare_payment_service.exception;

/**
 * Raised when the simulated payment gateway declines a payment. The declined attempt is
 * persisted with status FAILED so it can be audited and retried.
 */
public class PaymentFailedException extends RuntimeException {

    private final String paymentId;

    public PaymentFailedException(String message, String paymentId) {
        super(message);
        this.paymentId = paymentId;
    }

    public String getPaymentId() {
        return paymentId;
    }
}
