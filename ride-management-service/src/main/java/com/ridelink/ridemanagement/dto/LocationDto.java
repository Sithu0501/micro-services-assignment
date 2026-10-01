package com.ridelink.ridemanagement.dto;

import com.ridelink.ridemanagement.model.Location;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description="Location specification with address and coordinates")
public record LocationDto(
    @Schema(description="Human-readable address or landmark", example="SLIIT Malabe Campus, New Kandy Rd, Malabe")
    @NotBlank(message="Address must not be blank")
    String address,

    @Schema(description="WGS-84 Latitude (-90.0 to 90.0)", example="6.9147")
    @NotNull(message="Latitude is required")
    @DecimalMin(value="-90.0", message="Latitude must be >= -90.0")
    @DecimalMax(value="90.0", message="Latitude must be <= 90.0")
    Double latitude,

    @Schema(description="WGS-84 Longitude (-180.0 to 180.0)", example="79.9729")
    @NotNull(message="Longitude is required")
    @DecimalMin(value="-180.0", message="Longitude must be >= -180.0")
    @DecimalMax(value="180.0", message="Longitude must be <= 180.0")
    Double longitude
) {
    public static LocationDto fromEntity(Location location) {
        if (location == null) {
            return null;
        }
        return new LocationDto(location.getAddress(), location.getLatitude(), location.getLongitude());
    }

    public Location toEntity() {
        return new Location(this.address, this.latitude, this.longitude);
    }
}
