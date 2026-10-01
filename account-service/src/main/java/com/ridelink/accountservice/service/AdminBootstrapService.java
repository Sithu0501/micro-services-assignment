package com.ridelink.accountservice.service;

import com.ridelink.accountservice.model.AccountStatus;
import com.ridelink.accountservice.model.Role;
import com.ridelink.accountservice.model.User;
import com.ridelink.accountservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Admin Bootstrap Service – seeds an initial ADMIN account at application startup.
 *
 * This is a development-safe mechanism to obtain a working ADMIN account
 * for Swagger testing without allowing public ADMIN self-registration.
 *
 * <p>Configuration (via environment variables or .env file):
 * <pre>
 *   ADMIN_BOOTSTRAP_ENABLED=true          # Set to false to disable
 *   ADMIN_EMAIL=admin@ridelink.com        # Bootstrap admin email
 *   ADMIN_PASSWORD=Admin@RideLink2026!    # Bootstrap admin password (min requirements apply)
 *   ADMIN_FULL_NAME=RideLink Admin        # Bootstrap admin display name
 *   ADMIN_PHONE=+94770000001              # Bootstrap admin phone
 * </pre>
 *
 * <p>Behavior:
 * <ul>
 *   <li>Runs once at startup via ApplicationRunner.</li>
 *   <li>Checks if an ADMIN with the configured email already exists — does nothing if so.</li>
 *   <li>Creates the admin with BCrypt-hashed password and ACTIVE status.</li>
 *   <li>Never prints or logs the plaintext password.</li>
 *   <li>Disabled in test profile (admin.bootstrap.enabled=false).</li>
 * </ul>
 */
@Service
public class AdminBootstrapService implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.bootstrap.enabled:true}")
    private boolean bootstrapEnabled;

    @Value("${admin.bootstrap.email:admin@ridelink.com}")
    private String adminEmail;

    @Value("${admin.bootstrap.password:Admin@RideLink2026!}")
    private String adminPassword;

    @Value("${admin.bootstrap.full-name:RideLink Admin}")
    private String adminFullName;

    @Value("${admin.bootstrap.phone:+94770000001}")
    private String adminPhone;

    public AdminBootstrapService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Executes at application startup. Creates the configured admin account
     * if it does not yet exist.
     */
    @Override
    public void run(ApplicationArguments args) {
        if (!bootstrapEnabled) {
            log.debug("Admin bootstrap is disabled. Skipping admin seed.");
            return;
        }

        final String normalizedEmail = adminEmail.trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            log.info("Admin bootstrap: ADMIN account '{}' already exists. No action taken.", normalizedEmail);
            return;
        }

        // Hash password with BCrypt — plaintext is NEVER stored or logged
        final String encodedPassword = passwordEncoder.encode(adminPassword);

        User admin = new User(
                adminFullName.trim(),
                normalizedEmail,
                adminPhone.trim(),
                encodedPassword,
                Role.ADMIN,
                AccountStatus.ACTIVE
        );

        User saved = userRepository.save(admin);
        // Log only non-sensitive info — password NEVER appears in logs
        log.info("Admin bootstrap: ADMIN account created successfully. ID: {}, Email: {}", saved.getId(), saved.getEmail());
    }
}
