package com.ridelink.fare_payment_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public class FinalFareResponse {

    @Schema(example = "FAR-550e8400-e29b-41d4-a716-446655440000")
    private String fareId;

    @Schema(example = "RIDE-001")
    private String rideId;

    @Schema(example = "10.0")
    private Double distanceKm;

    @Schema(example = "900.00")
    private BigDecimal amount;

    @Schema(example = "LKR")
    private String currency;

    @Schema(example = "FINAL")
    private String fareType;

    public FinalFareResponse(
            String fareId,
            String rideId,
            Double distanceKm,
            BigDecimal amount,
            String currency,
            String fareType) {

        this.fareId = fareId;
        this.rideId = rideId;
        this.distanceKm = distanceKm;
        this.amount = amount;
        this.currency = currency;
        this.fareType = fareType;
    }

    public String getFareId() {
        return fareId;
    }

    public String getRideId() {
        return rideId;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getFareType() {
        return fareType;
    }
}