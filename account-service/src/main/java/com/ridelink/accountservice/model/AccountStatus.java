package com.ridelink.accountservice.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Account lifecycle statuses for users in the RideLink platform.
 * Governs authentication eligibility.
 */
@Schema(description = "User account lifecycle status")
public enum AccountStatus {
    @Schema(description = "Account is active and permitted to authenticate and access services")
    ACTIVE,

    @Schema(description = "Account is temporarily suspended by administrator; authentication blocked")
    SUSPENDED,

    @Schema(description = "Account has been deactivated; authentication blocked")
    DEACTIVATED
}
