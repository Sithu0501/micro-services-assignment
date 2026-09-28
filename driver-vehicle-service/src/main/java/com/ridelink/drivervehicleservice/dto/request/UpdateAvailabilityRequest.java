package com.ridelink.drivervehicleservice.dto.request;

import com.ridelink.drivervehicleservice.model.AvailabilityStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for updating driver operational availability.
 */
@Schema(description = "Driver availability update request")
public record UpdateAvailabilityRequest(
        @Schema(description = "New availability status", example = "AVAILABLE")
        @NotNull(message = "Availability status is required")
        AvailabilityStatus status
) {
}
