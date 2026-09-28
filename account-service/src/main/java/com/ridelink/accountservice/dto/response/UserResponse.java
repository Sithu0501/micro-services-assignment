package com.ridelink.accountservice.dto.response;

import com.ridelink.accountservice.model.AccountStatus;
import com.ridelink.accountservice.model.Role;
import com.ridelink.accountservice.model.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Safe user account projection exposed across API endpoints.
 * Explicitly excludes password hashes, secrets, and internal security metadata.
 */
@Schema(description = "Safe public projection of a User account")
public record UserResponse(
        @Schema(description = "Unique stable identifier of the user", example = "64f1a2b3c4d5e6f7a8b9c0d1")
        String id,

        @Schema(description = "User's full legal name", example = "Kasun Perera")
        String fullName,

        @Schema(description = "Registered email address (normalized lowercase)", example = "kasun.perera@example.com")
        String email,

        @Schema(description = "Sri Lankan mobile phone number in E.164 format", example = "+94771234567")
        String phone,

        @Schema(description = "Assigned user role (PASSENGER, DRIVER, ADMIN)", example = "PASSENGER")
        Role role,

        @Schema(description = "Account lifecycle status", example = "ACTIVE")
        AccountStatus status,

        @Schema(description = "Account creation timestamp (ISO-8601)", example = "2026-09-28T00:00:00Z")
        Instant createdAt,

        @Schema(description = "Account last update timestamp (ISO-8601)", example = "2026-09-28T00:00:00Z")
        Instant updatedAt
) {
    /**
     * Map a User MongoDB entity to a safe UserResponse DTO.
     *
     * @param user source User entity
     * @return safe UserResponse instance
     */
    public static UserResponse fromEntity(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
