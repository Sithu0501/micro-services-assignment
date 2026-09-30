package com.ridelink.fare_payment_service.dto;

import com.ridelink.fare_payment_service.entity.PaymentMethod;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PaymentRequest {

    @Schema(example = "RIDE-004")
    @NotBlank(message = "Ride ID is required")
    private String rideId;

    @Schema(
            example = "FAR-550e8400-e29b-41d4-a716-446655440000"
    )
    @NotBlank(message = "Fare ID is required")
    private String fareId;

    @Schema(example = "CARD_SIMULATED")
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    public String getRideId() {
        return rideId;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public String getFareId() {
        return fareId;
    }

    public void setFareId(String fareId) {
        this.fareId = fareId;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}