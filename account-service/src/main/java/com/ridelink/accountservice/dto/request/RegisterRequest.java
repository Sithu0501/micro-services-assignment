package com.ridelink.accountservice.dto.request;

import com.ridelink.accountservice.model.Role;
import com.ridelink.accountservice.util.ValidationUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Public registration payload.
 *
 * Registration rules:
 * - Default role assigned is PASSENGER if role is omitted.
 * - Allows DRIVER if explicitly requested.
 * - Clients cannot self-assign the ADMIN role; attempting to register as ADMIN yields HTTP 400 Bad Request.
 */
@Schema(description = "User registration request payload")
public record RegisterRequest(
        @Schema(description = "Full legal name of the user", example = "Kasun Perera", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Full name is required")
        @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
        String fullName,

        @Schema(description = "Unique email address for authentication", example = "kasun.perera@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @Schema(description = "Sri Lankan mobile phone number (e.g. +94771234567 or 0771234567)", example = "+94771234567", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = ValidationUtils.SRI_LANKAN_PHONE_REGEX, message = "Invalid Sri Lankan phone number. Must be a valid mobile number (+947XXXXXXXX, 07XXXXXXXX, or 7XXXXXXXX)")
        String phone,

        @Schema(description = "Account password (8-64 chars, min 1 upper, 1 lower, 1 digit, 1 special char)", example = "RideLink@2026", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#^()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/])[A-Za-z\\d@$!%*?&#^()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/]{8,64}$",
                message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character"
        )
        String password,

        @Schema(description = "Desired role (PASSENGER or DRIVER). ADMIN is not allowed through public registration. Defaults to PASSENGER if null.", example = "PASSENGER")
        Role role
) {}
