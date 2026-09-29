package com.ridelink.drivervehicleservice.dto.response;

import com.ridelink.drivervehicleservice.model.AvailabilityStatus;
import com.ridelink.drivervehicleservice.model.Driver;
import com.ridelink.drivervehicleservice.model.Location;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Response DTO projecting a Driver's operational profile.
 */
@Schema(description = "Driver operational profile response")
public record DriverResponse(
        @Schema(description = "Internal driver profile ID", example = "6501f2e8b4a2c10012ab34cd")
        String id,

        @Schema(description = "Account Service user ID", example = "6501f2e8b4a2c10012ab34ce")
        String userId,

        @Schema(description = "Driver's license number", example = "B1234567")
        String licenseNumber,

        @Schema(description = "Driver's license expiry date (YYYY-MM-DD)", example = "2028-12-31")
        String licenseExpiryDate,

        @Schema(description = "Operational phone number", example = "+94771234567")
        String phoneNumber,

        @Schema(description = "Operational availability status", example = "AVAILABLE")
        AvailabilityStatus availabilityStatus,

        @Schema(description = "Assigned operational service area", example = "Colombo")
        String serviceArea,

        @Schema(description = "Current simulated GPS coordinates")
        Location currentLocation,

        @Schema(description = "Profile creation timestamp", example = "2026-09-28T00:00:00Z")
        Instant createdAt,

        @Schema(description = "Profile last modified timestamp", example = "2026-09-28T00:00:00Z")
        Instant updatedAt
) {
    public static DriverResponse fromEntity(Driver driver) {
        if (driver == null) {
            return null;
        }
        return new DriverResponse(
                driver.getId(),
                driver.getUserId(),
                driver.getLicenseNumber(),
                driver.getLicenseExpiryDate(),
                driver.getPhoneNumber(),
                driver.getAvailabilityStatus(),
                driver.getServiceArea(),
                driver.getCurrentLocation(),
                driver.getCreatedAt(),
                driver.getUpdatedAt()
        );
    }
}
