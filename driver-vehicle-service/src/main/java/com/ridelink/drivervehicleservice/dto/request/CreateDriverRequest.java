package com.ridelink.drivervehicleservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request payload for creating a driver operational profile.
 */
@Schema(description = "Driver operational profile creation request")
public record CreateDriverRequest(
        @Schema(description = "Driver's license number", example = "B1234567")
        @NotBlank(message = "License number is required")
        @Size(min = 5, max = 30, message = "License number must be between 5 and 30 characters")
        String licenseNumber,

        @Schema(description = "Driver's license expiry date in YYYY-MM-DD format", example = "2028-12-31")
        @NotBlank(message = "License expiry date is required")
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "License expiry date must be in YYYY-MM-DD format")
        String licenseExpiryDate,

        @Schema(description = "Operational phone number (E.164 or national format)", example = "+94771234567")
        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone number must be a valid format between 10 and 15 digits")
        String phoneNumber,

        @Schema(description = "Operational service area / city", example = "Colombo")
        @NotBlank(message = "Service area is required")
        @Size(min = 2, max = 100, message = "Service area must be between 2 and 100 characters")
        String serviceArea
) {
}
