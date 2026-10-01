package com.ridelink.fare_payment_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.PositiveOrZero;

public class FinalFareRequest {

    @Schema(
            description = "Unique ride ID from Ride Management Service",
            example = "ride-12345"
    )
    @NotBlank(message = "Ride ID is required")
    private String rideId;

    @Schema(
            description = "Completed ride distance in kilometres",
            example = "10.0",
            minimum = "0"
    )
    @NotNull(message = "Distance is required")
    @PositiveOrZero(message = "Distance cannot be negative")
    @DecimalMax(value = "1000000.0", message = "Distance is too large")
    private Double distanceKm;

    public String getRideId() {
        return rideId;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }
}
