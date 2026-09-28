package com.ridelink.accountservice.service;

import com.ridelink.accountservice.dto.request.CreateUserRequest;
import com.ridelink.accountservice.dto.request.UpdateProfileRequest;
import com.ridelink.accountservice.dto.response.UserResponse;
import com.ridelink.accountservice.exception.DuplicateResourceException;
import com.ridelink.accountservice.exception.ResourceNotFoundException;
import com.ridelink.accountservice.model.AccountStatus;
import com.ridelink.accountservice.model.User;
import com.ridelink.accountservice.repository.UserRepository;
import com.ridelink.accountservice.util.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service managing user lifecycle, CRUD operations, profile management, and account status updates.
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Administrative account creation (Can assign any Role including ADMIN).
     *
     * @param request creation request
     * @return safe UserResponse
     */
    public UserResponse createUser(CreateUserRequest request) {
        final String normalizedEmail = ValidationUtils.normalizeEmail(request.email());
        log.info("Admin creating user account with email: {}, role: {}", normalizedEmail, request.role());

        if (userRepository.existsByEmail(normalizedEmail)) {
            log.warn("User creation rejected: Email '{}' already exists", normalizedEmail);
            throw new DuplicateResourceException("User with email '" + normalizedEmail + "' already exists");
        }

        final String normalizedPhone = ValidationUtils.normalizePhone(request.phone());
        final String encodedPassword = passwordEncoder.encode(request.password());
        final AccountStatus initialStatus = request.status() != null ? request.status() : AccountStatus.ACTIVE;

        User user = new User(
                request.fullName().trim(),
                normalizedEmail,
                normalizedPhone,
                encodedPassword,
                request.role(),
                initialStatus
        );

        User savedUser = userRepository.save(user);
        log.info("User created successfully by admin. ID: {}", savedUser.getId());
        return UserResponse.fromEntity(savedUser);
    }

    /**
     * Retrieve all registered users (Administrative operation).
     *
     * @return list of UserResponse objects
     */
    public List<UserResponse> getAllUsers() {
        log.debug("Fetching all users from repository");
        return userRepository.findAll()
                .stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    /**
     * Retrieve a specific user by identifier.
     * Enforces that non-admins can only view their own record.
     *
     * @param id                 target user id
     * @param authenticatedEmail email of caller
     * @param isAdmin            whether caller has ADMIN role
     * @return UserResponse
     */
    public UserResponse getUserById(String id, String authenticatedEmail, boolean isAdmin) {
        log.debug("Fetching user by ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (!isAdmin && !user.getEmail().equalsIgnoreCase(authenticatedEmail)) {
            log.warn("Unauthorized access attempt: User '{}' tried to view record of ID '{}'", authenticatedEmail, id);
            throw new AccessDeniedException("Access denied: You do not have permission to view other users' records");
        }

        return UserResponse.fromEntity(user);
    }

    /**
     * Retrieve profile of the currently authenticated user.
     *
     * @param authenticatedEmail caller's email
     * @return UserResponse
     */
    public UserResponse getCurrentUserProfile(String authenticatedEmail) {
        log.debug("Fetching current user profile for email: {}", authenticatedEmail);
        User user = userRepository.findByEmail(ValidationUtils.normalizeEmail(authenticatedEmail))
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", authenticatedEmail));

        return UserResponse.fromEntity(user);
    }

    /**
     * Update the currently authenticated user's profile.
     *
     * @param authenticatedEmail caller's email
     * @param request            profile updates
     * @return updated UserResponse
     */
    public UserResponse updateCurrentUserProfile(String authenticatedEmail, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(ValidationUtils.normalizeEmail(authenticatedEmail))
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", authenticatedEmail));

        return applyProfileUpdates(user, request);
    }

    /**
     * Update user by ID. Allows self-update or ADMIN update.
     * Sensitive fields (password, role, status) cannot be altered here.
     *
     * @param id                 target user id
     * @param request            profile update payload
     * @param authenticatedEmail email of caller
     * @param isAdmin            whether caller is admin
     * @return updated UserResponse
     */
    public UserResponse updateUser(String id, UpdateProfileRequest request, String authenticatedEmail, boolean isAdmin) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (!isAdmin && !user.getEmail().equalsIgnoreCase(authenticatedEmail)) {
            log.warn("Access denied: User '{}' tried to update user ID '{}'", authenticatedEmail, id);
            throw new AccessDeniedException("Access denied: You do not have permission to update this user's profile");
        }

        return applyProfileUpdates(user, request);
    }

    /**
     * Administrative endpoint to change account status (ACTIVE, SUSPENDED, DEACTIVATED).
     *
     * @param id        target user id
     * @param newStatus new account status
     * @return updated UserResponse
     */
    public UserResponse updateAccountStatus(String id, AccountStatus newStatus) {
        log.info("Updating account status for user ID '{}' to {}", id, newStatus);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        user.setStatus(newStatus);
        User updatedUser = userRepository.save(user);
        log.info("Account status updated successfully for user ID '{}'", id);

        return UserResponse.fromEntity(updatedUser);
    }

    /**
     * Administrative endpoint to delete a user account.
     *
     * @param id user id to delete
     */
    public void deleteUser(String id) {
        log.info("Administrative deletion requested for user ID '{}'", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        userRepository.delete(user);
        log.info("User ID '{}' successfully deleted", id);
    }

    private UserResponse applyProfileUpdates(User user, UpdateProfileRequest request) {
        if (request.fullName() != null && !request.fullName().isBlank()) {
            user.setFullName(request.fullName().trim());
        }

        if (request.phone() != null && !request.phone().isBlank()) {
            user.setPhone(ValidationUtils.normalizePhone(request.phone()));
        }

        if (request.email() != null && !request.email().isBlank()) {
            final String normalizedEmail = ValidationUtils.normalizeEmail(request.email());
            if (!normalizedEmail.equalsIgnoreCase(user.getEmail())) {
                if (userRepository.existsByEmail(normalizedEmail)) {
                    log.warn("Profile update conflict: Email '{}' already in use", normalizedEmail);
                    throw new DuplicateResourceException("An account with email '" + normalizedEmail + "' already exists");
                }
                user.setEmail(normalizedEmail);
            }
        }

        User updatedUser = userRepository.save(user);
        return UserResponse.fromEntity(updatedUser);
    }
}
