package com.ridelink.accountservice.repository;

import com.ridelink.accountservice.model.AccountStatus;
import com.ridelink.accountservice.model.Role;
import com.ridelink.accountservice.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for User entities.
 * Interacts solely with the 'users' collection inside 'ridelink_account_db'.
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {

    /**
     * Find a user by normalized lowercase email address.
     *
     * @param email normalized email address
     * @return Optional containing the User if found, otherwise empty
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if an active or registered user already exists with the given email.
     *
     * @param email normalized email address
     * @return true if a user exists with the email, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Find all users by role (e.g. PASSENGER, DRIVER, ADMIN).
     *
     * @param role user role
     * @return list of matching users
     */
    List<User> findByRole(Role role);

    /**
     * Find all users by account status (ACTIVE, SUSPENDED, DEACTIVATED).
     *
     * @param status account status
     * @return list of matching users
     */
    List<User> findByStatus(AccountStatus status);
}
