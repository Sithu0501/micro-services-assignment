package com.ridelink.drivervehicleservice.dto.request;

import com.ridelink.drivervehicleservice.model.VehicleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * Request payload for updating vehicle details.
 */
@Schema(description = "Vehicle update request")
public record UpdateVehicleRequest(
        @Schema(description = "Vehicle manufacturer", example = "Toyota")
        @Size(min = 2, max = 50, message = "Make must be between 2 and 50 characters")
        String make,

        @Schema(description = "Vehicle model", example = "Prius")
        @Size(min = 1, max = 50, message = "Model must be between 1 and 50 characters")
        String model,

        @Schema(description = "Manufacturing year", example = "2023")
        @Min(value = 1990, message = "Manufacturing year must be 1990 or newer")
        @Max(value = 2030, message = "Manufacturing year cannot exceed 2030")
        Integer year,

        @Schema(description = "Exterior color", example = "Metallic Silver")
        @Size(min = 2, max = 30, message = "Color must be between 2 and 30 characters")
        String color,

        @Schema(description = "Vehicle classification / category", example = "SEDAN")
        VehicleType vehicleType,

        @Schema(description = "Passenger seating capacity", example = "4")
        @Min(value = 1, message = "Capacity must be at least 1 passenger")
        @Max(value = 50, message = "Capacity cannot exceed 50 passengers")
        Integer capacity
) {
}
