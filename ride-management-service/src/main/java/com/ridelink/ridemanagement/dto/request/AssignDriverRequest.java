package com.ridelink.ridemanagement.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description="Payload to assign a driver to a requested ride")
public record AssignDriverRequest(
    @Schema(description="Driver operational profile ID (from Driver & Vehicle Service)", example="6501f2e8b4a2c10012ab34cd")
    @NotBlank(message="Driver ID must not be blank")
    String driverId,

    @Schema(description="Optional vehicle ID (if omitted, driver's registered vehicle is automatically resolved)", example="6501f2e8b4a2c10012ab34ce")
    String vehicleId
) {
}
