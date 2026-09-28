package com.ridelink.accountservice.service;

import com.ridelink.accountservice.dto.request.LoginRequest;
import com.ridelink.accountservice.dto.request.RegisterRequest;
import com.ridelink.accountservice.dto.response.LoginResponse;
import com.ridelink.accountservice.dto.response.RegisterResponse;
import com.ridelink.accountservice.dto.response.UserResponse;
import com.ridelink.accountservice.exception.AccountDisabledException;
import com.ridelink.accountservice.exception.BadRequestException;
import com.ridelink.accountservice.exception.DuplicateResourceException;
import com.ridelink.accountservice.exception.InvalidCredentialsException;
import com.ridelink.accountservice.model.AccountStatus;
import com.ridelink.accountservice.model.Role;
import com.ridelink.accountservice.model.User;
import com.ridelink.accountservice.repository.UserRepository;
import com.ridelink.accountservice.util.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service managing user authentication, registration, password verification,
 * and issuance of security tokens.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Register a new user account with default ACTIVE status.
     * Enforces that clients cannot self-assign ADMIN role through public registration.
     *
     * @param request validated registration details
     * @return RegisterResponse containing issued JWT and safe user details
     */
    public RegisterResponse register(RegisterRequest request) {
        final String normalizedEmail = ValidationUtils.normalizeEmail(request.email());
        log.info("Processing registration attempt for email: {}", normalizedEmail);

        // Security rule: Prevent public privilege escalation to ADMIN
        if (request.role() == Role.ADMIN) {
            log.warn("Blocked registration attempt requesting ADMIN role for email: {}", normalizedEmail);
            throw new BadRequestException("Registration with ADMIN role is not permitted via public registration");
        }

        // Uniqueness check
        if (userRepository.existsByEmail(normalizedEmail)) {
            log.warn("Registration rejected: Duplicate email '{}'", normalizedEmail);
            throw new DuplicateResourceException("An account with email '" + normalizedEmail + "' already exists");
        }

        // Normalize phone number according to Sri Lankan mobile standards
        final String normalizedPhone = ValidationUtils.normalizePhone(request.phone());

        // Assign role: default to PASSENGER if omitted, or DRIVER if explicitly requested
        final Role assignedRole = (request.role() == Role.DRIVER) ? Role.DRIVER : Role.PASSENGER;

        // Hash password securely using BCrypt (never stored in plaintext)
        final String encodedPassword = passwordEncoder.encode(request.password());

        // Persist User entity in ridelink_account_db.users
        User user = new User(
                request.fullName().trim(),
                normalizedEmail,
                normalizedPhone,
                encodedPassword,
                assignedRole,
                AccountStatus.ACTIVE
        );

        User savedUser = userRepository.save(user);
        log.info("User registered successfully. ID: {}, Email: {}, Role: {}", savedUser.getId(), savedUser.getEmail(), savedUser.getRole());

        // Generate stateless JWT token
        String token = jwtService.generateToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole());

        return new RegisterResponse(token, jwtService.getJwtExpiration(), UserResponse.fromEntity(savedUser));
    }

    /**
     * Authenticate existing user with email and password.
     * Verifies account status (SUSPENDED / DEACTIVATED) prior to token issuance.
     *
     * @param request login credentials
     * @return LoginResponse containing issued JWT and safe user details
     */
    public LoginResponse login(LoginRequest request) {
        final String normalizedEmail = ValidationUtils.normalizeEmail(request.email());
        log.info("Processing login attempt for email: {}", normalizedEmail);

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> {
                    log.warn("Authentication failed: User with email '{}' not found", normalizedEmail);
                    return new InvalidCredentialsException("Invalid email or password");
                });

        // Enforce account status check before password check to provide clear feedback
        if (user.getStatus() == AccountStatus.SUSPENDED) {
            log.warn("Authentication blocked: Account '{}' is SUSPENDED", normalizedEmail);
            throw new AccountDisabledException("Your account has been suspended. Please contact customer support.");
        }

        if (user.getStatus() == AccountStatus.DEACTIVATED) {
            log.warn("Authentication blocked: Account '{}' is DEACTIVATED", normalizedEmail);
            throw new AccountDisabledException("Your account is deactivated. Please contact customer support.");
        }

        // Verify password against BCrypt hash
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            log.warn("Authentication failed: Password mismatch for user '{}'", normalizedEmail);
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // Generate stateless JWT token
        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole());
        log.info("User logged in successfully. ID: {}, Role: {}", user.getId(), user.getRole());

        return new LoginResponse(token, jwtService.getJwtExpiration(), UserResponse.fromEntity(user));
    }
}
