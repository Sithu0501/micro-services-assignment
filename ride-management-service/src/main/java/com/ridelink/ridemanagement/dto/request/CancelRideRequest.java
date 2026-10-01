package com.ridelink.ridemanagement.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description="Payload to cancel a ride")
public record CancelRideRequest(
    @Schema(description="Reason for cancellation", example="Change of plans, found alternate transport")
    @NotBlank(message="Cancellation reason must not be blank")
    @Size(max=500, message="Cancellation reason must not exceed 500 characters")
    String reason
) {
}
