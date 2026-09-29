package com.ridelink.accountservice.dto.request;

import com.ridelink.accountservice.util.ValidationUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Payload for updating user profile information.
 * Sensitive fields (id, password, role, status, timestamps) are strictly immutable through this endpoint.
 */
@Schema(description = "Profile update request payload")
public record UpdateProfileRequest(
        @Schema(description = "Updated full name", example = "Kasun M. Perera")
        @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
        String fullName,

        @Schema(description = "Updated email address", example = "kasun.new@example.com")
        @Email(message = "Invalid email format")
        String email,

        @Schema(description = "Updated Sri Lankan mobile number", example = "+94779876543")
        @Pattern(regexp = ValidationUtils.SRI_LANKAN_PHONE_REGEX, message = "Invalid Sri Lankan phone number format. Must be like +947XXXXXXXX or 07XXXXXXXX")
        String phone
) {}
