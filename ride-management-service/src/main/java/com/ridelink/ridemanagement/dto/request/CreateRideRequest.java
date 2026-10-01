package com.ridelink.ridemanagement.dto.request;

import com.ridelink.ridemanagement.dto.LocationDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Schema(description="Payload to request a new ride")
public record CreateRideRequest(
    @Schema(description="Account Service passenger ID (optional; defaults to the authenticated user's ID)", example="64f1a2b3c4d5e6f7a8b9c0d1")
    String passengerId,

    @Schema(description="Origin / pickup location details")
    @NotNull(message="Pickup location is required")
    @Valid
    LocationDto pickupLocation,

    @Schema(description="Destination location details")
    @NotNull(message="Destination location is required")
    @Valid
    LocationDto destinationLocation
) {
}
