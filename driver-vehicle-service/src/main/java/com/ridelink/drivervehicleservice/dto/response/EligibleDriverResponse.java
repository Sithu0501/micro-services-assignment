package com.ridelink.drivervehicleservice.dto.response;

import com.ridelink.drivervehicleservice.model.AvailabilityStatus;
import com.ridelink.drivervehicleservice.model.Location;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO projecting an available, eligible driver for dispatch by Ride Management Service.
 */
@Schema(description = "Eligible driver projection for ride matching")
public record EligibleDriverResponse(
        @Schema(description = "Driver operational profile ID", example = "6501f2e8b4a2c10012ab34cd")
        String driverId,

        @Schema(description = "Account Service user ID", example = "6501f2e8b4a2c10012ab34ce")
        String userId,

        @Schema(description = "Driver's license number", example = "B1234567")
        String licenseNumber,

        @Schema(description = "Operational contact phone number", example = "+94771234567")
        String phoneNumber,

        @Schema(description = "Operational availability status", example = "AVAILABLE")
        AvailabilityStatus availabilityStatus,

        @Schema(description = "Assigned operational service area", example = "Colombo")
        String serviceArea,

        @Schema(description = "Current simulated GPS coordinates")
        Location currentLocation,

        @Schema(description = "Calculated distance from search location in kilometers", example = "2.3")
        Double distanceKm,

        @Schema(description = "Registered vehicle details for this driver")
        VehicleResponse vehicle
) {
}
