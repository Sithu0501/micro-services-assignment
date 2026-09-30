package com.ridelink.fare_payment_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public class FareEstimateResponse {

    @Schema(example = "Beliatta")
    private String pickup;

    @Schema(example = "Matara")
    private String destination;

    @Schema(example = "10.0")
    private Double distanceKm;

    @Schema(
            description = "Fixed base fare in LKR",
            example = "100.00"
    )
    private BigDecimal baseFare;

    @Schema(
            description = "Fare charged per kilometre",
            example = "80.00"
    )
    private BigDecimal ratePerKm;

    @Schema(
            description = "Calculated estimated fare",
            example = "900.00"
    )
    private BigDecimal amount;

    @Schema(example = "LKR")
    private String currency;

    @Schema(example = "ESTIMATE")
    private String type;

    public FareEstimateResponse(
            String pickup,
            String destination,
            Double distanceKm,
            BigDecimal baseFare,
            BigDecimal ratePerKm,
            BigDecimal amount,
            String currency,
            String type) {

        this.pickup = pickup;
        this.destination = destination;
        this.distanceKm = distanceKm;
        this.baseFare = baseFare;
        this.ratePerKm = ratePerKm;
        this.amount = amount;
        this.currency = currency;
        this.type = type;
    }

    public String getPickup() {
        return pickup;
    }

    public String getDestination() {
        return destination;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public BigDecimal getBaseFare() {
        return baseFare;
    }

    public BigDecimal getRatePerKm() {
        return ratePerKm;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getType() {
        return type;
    }
}