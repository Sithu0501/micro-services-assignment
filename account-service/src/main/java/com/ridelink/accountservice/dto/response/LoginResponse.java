package com.ridelink.accountservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response payload returned upon successful user authentication.
 */
@Schema(description = "Login authentication response containing JWT token and user account information")
public record LoginResponse(
        @Schema(description = "Signed JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken,

        @Schema(description = "Token type scheme", example = "Bearer")
        String tokenType,

        @Schema(description = "Token lifespan in milliseconds", example = "86400000")
        long expiresIn,

        @Schema(description = "Authenticated user account details")
        UserResponse user
) {
    public LoginResponse(String accessToken, long expiresIn, UserResponse user) {
        this(accessToken, "Bearer", expiresIn, user);
    }
}
