package com.ridelink.accountservice.dto.request;

import com.ridelink.accountservice.model.AccountStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Administrative payload for modifying a user account's status.
 */
@Schema(description = "Account status update payload")
public record UpdateAccountStatusRequest(
        @Schema(description = "New lifecycle status (ACTIVE, SUSPENDED, DEACTIVATED)", example = "SUSPENDED", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Account status is required")
        AccountStatus status
) {}
