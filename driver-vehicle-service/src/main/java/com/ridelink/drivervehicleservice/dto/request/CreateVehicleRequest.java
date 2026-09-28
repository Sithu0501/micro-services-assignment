package com.ridelink.drivervehicleservice.dto.request;

import com.ridelink.drivervehicleservice.model.VehicleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request payload for registering a new vehicle.
 */
@Schema(description = "Vehicle registration request")
public record CreateVehicleRequest(
        @Schema(description = "License plate / registration number", example = "CAB-1234")
        @NotBlank(message = "Registration number is required")
        @Size(min = 2, max = 20, message = "Registration number must be between 2 and 20 characters")
        String registrationNumber,

        @Schema(description = "Vehicle manufacturer", example = "Toyota")
        @NotBlank(message = "Vehicle make is required")
        @Size(min = 2, max = 50, message = "Make must be between 2 and 50 characters")
        String make,

        @Schema(description = "Vehicle model", example = "Prius")
        @NotBlank(message = "Vehicle model is required")
        @Size(min = 1, max = 50, message = "Model must be between 1 and 50 characters")
        String model,

        @Schema(description = "Manufacturing year", example = "2022")
        @NotNull(message = "Manufacturing year is required")
        @Min(value = 1990, message = "Manufacturing year must be 1990 or newer")
        @Max(value = 2030, message = "Manufacturing year cannot exceed 2030")
        Integer year,

        @Schema(description = "Exterior color", example = "Pearl White")
        @NotBlank(message = "Color is required")
        @Size(min = 2, max = 30, message = "Color must be between 2 and 30 characters")
        String color,

        @Schema(description = "Vehicle classification / category", example = "SEDAN")
        @NotNull(message = "Vehicle type is required")
        VehicleType vehicleType,

        @Schema(description = "Passenger seating capacity", example = "4")
        @NotNull(message = "Passenger capacity is required")
        @Min(value = 1, message = "Capacity must be at least 1 passenger")
        @Max(value = 50, message = "Capacity cannot exceed 50 passengers")
        Integer capacity
) {
}
