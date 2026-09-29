package com.ridelink.drivervehicleservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request payload for updating driver operational details.
 */
@Schema(description = "Driver operational profile update request")
public record UpdateDriverRequest(
        @Schema(description = "Updated license expiry date in YYYY-MM-DD format", example = "2029-12-31")
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "License expiry date must be in YYYY-MM-DD format")
        String licenseExpiryDate,

        @Schema(description = "Updated operational phone number", example = "+94779876543")
        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone number must be a valid format between 10 and 15 digits")
        String phoneNumber,

        @Schema(description = "Updated operational service area / city", example = "Kandy")
        @Size(min = 2, max = 100, message = "Service area must be between 2 and 100 characters")
        String serviceArea
) {
}
