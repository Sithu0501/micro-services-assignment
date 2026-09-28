package com.ridelink.accountservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response payload returned upon successful user registration.
 */
@Schema(description = "Registration success payload including JWT authentication token and user profile")
public record RegisterResponse(
        @Schema(description = "Signed JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken,

        @Schema(description = "Token type scheme", example = "Bearer")
        String tokenType,

        @Schema(description = "Token lifespan in milliseconds", example = "86400000")
        long expiresIn,

        @Schema(description = "Registered user account profile")
        UserResponse user
) {
    public RegisterResponse(String accessToken, long expiresIn, UserResponse user) {
        this(accessToken, "Bearer", expiresIn, user);
    }
}
