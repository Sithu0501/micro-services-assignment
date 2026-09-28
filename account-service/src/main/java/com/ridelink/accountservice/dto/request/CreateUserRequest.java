package com.ridelink.accountservice.dto.request;

import com.ridelink.accountservice.model.AccountStatus;
import com.ridelink.accountservice.model.Role;
import com.ridelink.accountservice.util.ValidationUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Administrative payload for creating any user account directly.
 * Can specify PASSENGER, DRIVER, or ADMIN roles.
 */
@Schema(description = "Admin user creation payload")
public record CreateUserRequest(
        @Schema(description = "Full name of the user", example = "Saman Kumara", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Full name is required")
        @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
        String fullName,

        @Schema(description = "Unique email address", example = "saman.admin@ridelink.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @Schema(description = "Sri Lankan mobile phone number", example = "+94712345678", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = ValidationUtils.SRI_LANKAN_PHONE_REGEX, message = "Invalid Sri Lankan phone number format")
        String phone,

        @Schema(description = "Account password", example = "Admin@2026!", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#^()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/])[A-Za-z\\d@$!%*?&#^()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/]{8,64}$",
                message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character"
        )
        String password,

        @Schema(description = "Assigned role", example = "ADMIN", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Role is required")
        Role role,

        @Schema(description = "Initial status (defaults to ACTIVE if null)", example = "ACTIVE")
        AccountStatus status
) {}
