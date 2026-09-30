package com.ridelink.fare_payment_service.dto;

import com.ridelink.fare_payment_service.entity.PaymentMethod;
import com.ridelink.fare_payment_service.entity.PaymentStatus;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

public class ReceiptResponse {

    @Schema(example = "RIDE-004")
    private String rideId;

    @Schema(example = "FAR-550e8400-e29b-41d4-a716-446655440000")
    private String fareId;

    @Schema(example = "PAY-550e8400-e29b-41d4-a716-446655440000")
    private String paymentId;

    @Schema(example = "900.00")
    private BigDecimal amount;

    @Schema(example = "LKR")
    private String currency;

    @Schema(example = "CARD_SIMULATED")
    private PaymentMethod paymentMethod;

    @Schema(example = "SUCCESS")
    private PaymentStatus paymentStatus;

    @Schema(example = "TXN-550e8400-e29b-41d4-a716-446655440000")
    private String transactionReference;

    private Instant paidAt;

    public ReceiptResponse(
            String rideId,
            String fareId,
            String paymentId,
            BigDecimal amount,
            String currency,
            PaymentMethod paymentMethod,
            PaymentStatus paymentStatus,
            String transactionReference,
            Instant paidAt) {

        this.rideId = rideId;
        this.fareId = fareId;
        this.paymentId = paymentId;
        this.amount = amount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.transactionReference = transactionReference;
        this.paidAt = paidAt;
    }

    public String getRideId() {
        return rideId;
    }

    public String getFareId() {
        return fareId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public Instant getPaidAt() {
        return paidAt;
    }
}