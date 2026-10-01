package com.ridelink.ridemanagement.dto.request;

import com.ridelink.ridemanagement.dto.LocationDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Schema(description="Payload to update an editable ride request")
public record UpdateRideRequest(
    @Schema(description="Updated pickup location")
    @NotNull(message="Pickup location is required")
    @Valid
    LocationDto pickupLocation,

    @Schema(description="Updated destination location")
    @NotNull(message="Destination location is required")
    @Valid
    LocationDto destinationLocation
) {
}
