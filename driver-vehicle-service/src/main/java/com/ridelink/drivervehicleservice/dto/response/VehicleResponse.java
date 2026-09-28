package com.ridelink.drivervehicleservice.dto.response;

import com.ridelink.drivervehicleservice.model.Vehicle;
import com.ridelink.drivervehicleservice.model.VehicleType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Response DTO projecting Vehicle details.
 */
@Schema(description = "Vehicle details response")
public record VehicleResponse(
        @Schema(description = "Internal vehicle ID", example = "6501f2e8b4a2c10012ab34ce")
        String id,

        @Schema(description = "Associated driver profile ID", example = "6501f2e8b4a2c10012ab34cd")
        String driverId,

        @Schema(description = "License plate / registration number", example = "CAB-1234")
        String registrationNumber,

        @Schema(description = "Vehicle manufacturer", example = "Toyota")
        String make,

        @Schema(description = "Vehicle model", example = "Prius")
        String model,

        @Schema(description = "Manufacturing year", example = "2022")
        int year,

        @Schema(description = "Exterior color", example = "Pearl White")
        String color,

        @Schema(description = "Vehicle classification / category", example = "SEDAN")
        VehicleType vehicleType,

        @Schema(description = "Passenger seating capacity", example = "4")
        int capacity,

        @Schema(description = "Registration timestamp", example = "2026-09-28T00:00:00Z")
        Instant createdAt,

        @Schema(description = "Last modified timestamp", example = "2026-09-28T00:00:00Z")
        Instant updatedAt
) {
    public static VehicleResponse fromEntity(Vehicle vehicle) {
        if (vehicle == null) {
            return null;
        }
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getDriverId(),
                vehicle.getRegistrationNumber(),
                vehicle.getMake(),
                vehicle.getModel(),
                vehicle.getYear(),
                vehicle.getColor(),
                vehicle.getVehicleType(),
                vehicle.getCapacity(),
                vehicle.getCreatedAt(),
                vehicle.getUpdatedAt()
        );
    }
}
