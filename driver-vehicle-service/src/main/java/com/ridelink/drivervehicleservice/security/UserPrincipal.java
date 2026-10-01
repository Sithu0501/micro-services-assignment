package com.ridelink.drivervehicleservice.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Custom UserDetails principal populated directly from verified JWT claims.
 * Allows controllers and services to access the user's ID, email, and roles
 * statelessly.
 */
public class UserPrincipal implements UserDetails {

    private final String userId;
    private final String email;
    private final String role;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(String userId, String email, String role) {
        this.userId = userId;
        this.email = email;
        this.role = role != null ? role.toUpperCase() : "DRIVER";
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + this.role));
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null; // Stateless JWT, no password needed
    }

    @Override
    public String getUsername() {
        return email != null ? email : userId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserPrincipal that = (UserPrincipal) o;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return "UserPrincipal{" +
                "userId='" + userId + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
