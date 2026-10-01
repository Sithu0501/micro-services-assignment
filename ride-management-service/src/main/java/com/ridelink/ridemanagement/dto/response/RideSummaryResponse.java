/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.swagger.v3.oas.annotations.media.Schema
 */
package com.ridelink.ridemanagement.dto.response;

import com.ridelink.ridemanagement.model.Ride;
import com.ridelink.ridemanagement.model.RideStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description="Compact ride summary")
public record RideSummaryResponse(@Schema(description="Unique ride identifier", example="6501f2e8b4a2c10012ab34cd") String id, @Schema(description="Passenger user ID", example="64f1a2b3c4d5e6f7a8b9c0d1") String passengerId, @Schema(description="Assigned driver profile ID", example="6501f2e8b4a2c10012ab34cd") String driverId, @Schema(description="Ride status", example="COMPLETED") RideStatus status, @Schema(description="Pickup street address", example="SLIIT Malabe Campus") String pickupAddress, @Schema(description="Destination street address", example="Colombo Fort") String destinationAddress, @Schema(description="Requested timestamp", example="2026-10-01T10:00:00Z") Instant requestedAt, @Schema(description="Completion timestamp (if completed)", example="2026-10-01T10:25:00Z") Instant completedAt) {
    public static RideSummaryResponse fromEntity(Ride ride) {
        if (ride == null) {
            return null;
        }
        return new RideSummaryResponse(ride.getId(), ride.getPassengerId(), ride.getDriverId(), ride.getStatus(), ride.getPickupLocation() != null ? ride.getPickupLocation().getAddress() : null, ride.getDestinationLocation() != null ? ride.getDestinationLocation().getAddress() : null, ride.getRequestedAt(), ride.getCompletedAt());
    }
}

