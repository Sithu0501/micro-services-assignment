package com.ridelink.ridemanagement.dto.request;

import com.ridelink.ridemanagement.model.RideStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description="Payload to update ride lifecycle status")
public record UpdateRideStatusRequest(
    @Schema(description="Target ride lifecycle status", example="ACCEPTED")
    @NotNull(message="Ride status must not be null")
    RideStatus status,

    @Schema(description="Optional operational note or explanation", example="Driver accepted dispatch")
    String note
) {
}
