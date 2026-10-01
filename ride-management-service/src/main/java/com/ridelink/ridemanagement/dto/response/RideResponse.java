/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.swagger.v3.oas.annotations.media.Schema
 */
package com.ridelink.ridemanagement.dto.response;

import com.ridelink.ridemanagement.dto.LocationDto;
import com.ridelink.ridemanagement.model.Ride;
import com.ridelink.ridemanagement.model.RideStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description="Detailed ride response projection")
public record RideResponse(@Schema(description="Unique ride identifier", example="6501f2e8b4a2c10012ab34cd") String id, @Schema(description="Account Service passenger ID", example="64f1a2b3c4d5e6f7a8b9c0d1") String passengerId, @Schema(description="Driver operational profile ID (null if not yet assigned)", example="6501f2e8b4a2c10012ab34cd") String driverId, @Schema(description="Assigned vehicle ID (null if not yet assigned)", example="6501f2e8b4a2c10012ab34ce") String vehicleId, @Schema(description="Pickup location details") LocationDto pickupLocation, @Schema(description="Destination location details") LocationDto destinationLocation, @Schema(description="Current lifecycle status of the ride", example="REQUESTED") RideStatus status, @Schema(description="Timestamp when the ride was requested (ISO-8601)", example="2026-10-01T10:00:00Z") Instant requestedAt, @Schema(description="Timestamp when a driver was assigned (ISO-8601)", example="2026-10-01T10:02:00Z") Instant assignedAt, @Schema(description="Timestamp when the driver accepted the ride (ISO-8601)", example="2026-10-01T10:03:00Z") Instant acceptedAt, @Schema(description="Timestamp when the trip started (ISO-8601)", example="2026-10-01T10:05:00Z") Instant startedAt, @Schema(description="Timestamp when the trip completed (ISO-8601)", example="2026-10-01T10:25:00Z") Instant completedAt, @Schema(description="Timestamp when the ride was cancelled (ISO-8601)", example="2026-10-01T10:04:00Z") Instant cancelledAt, @Schema(description="Reason provided if cancelled", example="Change of plans") String cancellationReason, @Schema(description="Record creation timestamp (ISO-8601)", example="2026-10-01T10:00:00Z") Instant createdAt, @Schema(description="Record last modified timestamp (ISO-8601)", example="2026-10-01T10:00:00Z") Instant updatedAt) {
    public static RideResponse fromEntity(Ride ride) {
        if (ride == null) {
            return null;
        }
        return new RideResponse(ride.getId(), ride.getPassengerId(), ride.getDriverId(), ride.getVehicleId(), LocationDto.fromEntity(ride.getPickupLocation()), LocationDto.fromEntity(ride.getDestinationLocation()), ride.getStatus(), ride.getRequestedAt(), ride.getAssignedAt(), ride.getAcceptedAt(), ride.getStartedAt(), ride.getCompletedAt(), ride.getCancelledAt(), ride.getCancellationReason(), ride.getCreatedAt(), ride.getUpdatedAt());
    }
}

