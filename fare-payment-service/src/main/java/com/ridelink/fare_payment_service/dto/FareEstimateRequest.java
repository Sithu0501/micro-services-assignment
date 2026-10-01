package com.ridelink.fare_payment_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.PositiveOrZero;

public class FareEstimateRequest {

    @Schema(
            description = "Pickup location of the ride",
            example = "Beliatta"
    )
    @NotBlank(message = "Pickup location is required")
    private String pickup;

    @Schema(
            description = "Destination location of the ride",
            example = "Matara"
    )
    @NotBlank(message = "Destination is required")
    private String destination;

    @Schema(
            description = "Simulated ride distance in kilometres",
            example = "10.0",
            minimum = "0"
    )
    @NotNull(message = "Distance is required")
    @PositiveOrZero(message = "Distance cannot be negative")
    @DecimalMax(value = "1000000.0", message = "Distance is too large")
    private Double distanceKm;

    public String getPickup() {
        return pickup;
    }

    public void setPickup(String pickup) {
        this.pickup = pickup;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }
}
