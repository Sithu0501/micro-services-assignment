package com.ridelink.accountservice.service;

import com.ridelink.accountservice.dto.request.CreateUserRequest;
import com.ridelink.accountservice.dto.request.UpdateProfileRequest;
import com.ridelink.accountservice.dto.response.UserResponse;
import com.ridelink.accountservice.exception.DuplicateResourceException;
import com.ridelink.accountservice.exception.ResourceNotFoundException;
import com.ridelink.accountservice.model.AccountStatus;
import com.ridelink.accountservice.model.Role;
import com.ridelink.accountservice.model.User;
import com.ridelink.accountservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    @DisplayName("Admin creates user successfully with ADMIN role")
    void testCreateUserAdmin() {
        CreateUserRequest request = new CreateUserRequest(
                "System Admin",
                "admin@ridelink.com",
                "0771234567",
                "AdminPass@123",
                Role.ADMIN,
                AccountStatus.ACTIVE
        );

        when(userRepository.existsByEmail("admin@ridelink.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-pwd");

        User savedUser = new User("admin-id-1", "System Admin", "admin@ridelink.com", "+94771234567", "hashed-pwd", Role.ADMIN, AccountStatus.ACTIVE);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = userService.createUser(request);

        assertNotNull(response);
        assertEquals("admin-id-1", response.id());
        assertEquals(Role.ADMIN, response.role());
    }

    @Test
    @DisplayName("Create user fails if email is already in use")
    void testCreateUserDuplicateEmail() {
        CreateUserRequest request = new CreateUserRequest(
                "Duplicate User",
                "existing@ridelink.com",
                "0771234567",
                "Password@123",
                Role.PASSENGER,
                AccountStatus.ACTIVE
        );

        when(userRepository.existsByEmail("existing@ridelink.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.createUser(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Get all users returns mapped UserResponse list")
    void testGetAllUsers() {
        User u1 = new User("id1", "User One", "user1@ridelink.com", "+94771111111", "pwd", Role.PASSENGER, AccountStatus.ACTIVE);
        User u2 = new User("id2", "User Two", "user2@ridelink.com", "+94772222222", "pwd", Role.DRIVER, AccountStatus.ACTIVE);

        when(userRepository.findAll()).thenReturn(List.of(u1, u2));

        List<UserResponse> list = userService.getAllUsers();

        assertEquals(2, list.size());
        assertEquals("user1@ridelink.com", list.get(0).email());
        assertEquals("user2@ridelink.com", list.get(1).email());
    }

    @Test
    @DisplayName("Get user by ID succeeds when caller is admin")
    void testGetUserByIdAsAdmin() {
        User targetUser = new User("uid-target", "Target User", "target@ridelink.com", "+94771234567", "pwd", Role.PASSENGER, AccountStatus.ACTIVE);
        when(userRepository.findById("uid-target")).thenReturn(Optional.of(targetUser));

        UserResponse response = userService.getUserById("uid-target", "admin@ridelink.com", true);

        assertNotNull(response);
        assertEquals("uid-target", response.id());
    }

    @Test
    @DisplayName("Get user by ID succeeds when caller is target user themselves")
    void testGetUserByIdAsSelf() {
        User self = new User("uid-self", "Self User", "self@ridelink.com", "+94771234567", "pwd", Role.PASSENGER, AccountStatus.ACTIVE);
        when(userRepository.findById("uid-self")).thenReturn(Optional.of(self));

        UserResponse response = userService.getUserById("uid-self", "self@ridelink.com", false);

        assertNotNull(response);
        assertEquals("uid-self", response.id());
    }

    @Test
    @DisplayName("Get user by ID fails with AccessDeniedException when non-admin accesses someone else's record")
    void testGetUserByIdUnauthorized() {
        User targetUser = new User("uid-other", "Other User", "other@ridelink.com", "+94771234567", "pwd", Role.PASSENGER, AccountStatus.ACTIVE);
        when(userRepository.findById("uid-other")).thenReturn(Optional.of(targetUser));

        assertThrows(AccessDeniedException.class, () -> userService.getUserById("uid-other", "impostor@ridelink.com", false));
    }

    @Test
    @DisplayName("Get non-existent user throws ResourceNotFoundException")
    void testGetUserNotFound() {
        when(userRepository.findById("unknown-id")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById("unknown-id", "admin@ridelink.com", true));
    }

    @Test
    @DisplayName("Update user profile updates allowed fields and normalizes phone")
    void testUpdateProfileSuccess() {
        User user = new User("uid-100", "Old Name", "user@ridelink.com", "+94771111111", "pwd", Role.PASSENGER, AccountStatus.ACTIVE);
        when(userRepository.findByEmail("user@ridelink.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UpdateProfileRequest request = new UpdateProfileRequest("New Name", null, "0772222222");
        UserResponse response = userService.updateCurrentUserProfile("user@ridelink.com", request);

        assertEquals("New Name", response.fullName());
        assertEquals("+94772222222", response.phone());
    }

    @Test
    @DisplayName("Update user profile with duplicate email throws DuplicateResourceException")
    void testUpdateProfileDuplicateEmail() {
        User user = new User("uid-100", "User Name", "user@ridelink.com", "+94771111111", "pwd", Role.PASSENGER, AccountStatus.ACTIVE);
        when(userRepository.findByEmail("user@ridelink.com")).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("taken@ridelink.com")).thenReturn(true);

        UpdateProfileRequest request = new UpdateProfileRequest(null, "taken@ridelink.com", null);

        assertThrows(DuplicateResourceException.class, () -> userService.updateCurrentUserProfile("user@ridelink.com", request));
    }

    @Test
    @DisplayName("Update account status to SUSPENDED modifies status correctly")
    void testUpdateAccountStatus() {
        User user = new User("uid-100", "User Name", "user@ridelink.com", "+94771111111", "pwd", Role.PASSENGER, AccountStatus.ACTIVE);
        when(userRepository.findById("uid-100")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserResponse response = userService.updateAccountStatus("uid-100", AccountStatus.SUSPENDED);

        assertEquals(AccountStatus.SUSPENDED, response.status());
    }

    @Test
    @DisplayName("Delete user deletes record from database")
    void testDeleteUserSuccess() {
        User user = new User("uid-100", "User Name", "user@ridelink.com", "+94771111111", "pwd", Role.PASSENGER, AccountStatus.ACTIVE);
        when(userRepository.findById("uid-100")).thenReturn(Optional.of(user));

        userService.deleteUser("uid-100");

        verify(userRepository).delete(user);
    }

    @Test
    @DisplayName("Delete non-existent user throws ResourceNotFoundException")
    void testDeleteUserNotFound() {
        when(userRepository.findById("uid-unknown")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser("uid-unknown"));
        verify(userRepository, never()).delete(any(User.class));
    }
}
