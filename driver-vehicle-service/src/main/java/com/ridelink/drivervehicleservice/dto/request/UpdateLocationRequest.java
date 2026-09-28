package com.ridelink.drivervehicleservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for updating simulated driver GPS coordinates.
 */
@Schema(description = "Driver simulated location update request")
public record UpdateLocationRequest(
        @Schema(description = "Latitude (-90.0 to 90.0)", example = "6.9271")
        @NotNull(message = "Latitude is required")
        @DecimalMin(value = "-90.0", message = "Latitude must be greater than or equal to -90.0")
        @DecimalMax(value = "90.0", message = "Latitude must be less than or equal to 90.0")
        Double latitude,

        @Schema(description = "Longitude (-180.0 to 180.0)", example = "79.8612")
        @NotNull(message = "Longitude is required")
        @DecimalMin(value = "-180.0", message = "Longitude must be greater than or equal to -180.0")
        @DecimalMax(value = "180.0", message = "Longitude must be less than or equal to 180.0")
        Double longitude
) {
}
