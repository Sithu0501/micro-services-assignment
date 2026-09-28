package com.ridelink.accountservice.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * User roles in the RideLink platform.
 * Supports Role-Based Access Control (RBAC).
 */
@Schema(description = "User role within RideLink system")
public enum Role {
    @Schema(description = "Regular passenger requesting rides")
    PASSENGER,

    @Schema(description = "Registered driver offering rides")
    DRIVER,

    @Schema(description = "System administrator with full management access")
    ADMIN
}
