/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.springframework.security.core.GrantedAuthority
 *  org.springframework.security.core.authority.SimpleGrantedAuthority
 *  org.springframework.security.core.userdetails.UserDetails
 */
package com.ridelink.ridemanagement.security;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserPrincipal
implements UserDetails {
    private final String userId;
    private final String email;
    private final String role;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(String userId, String email, String role) {
        this.userId = userId;
        this.email = email;
        this.role = role != null ? role.toUpperCase() : "PASSENGER";
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + this.role));
    }

    public String getUserId() {
        return this.userId;
    }

    public String getEmail() {
        return this.email;
    }

    public String getRole() {
        return this.role;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(this.role);
    }

    public boolean isPassenger() {
        return "PASSENGER".equalsIgnoreCase(this.role);
    }

    public boolean isDriver() {
        return "DRIVER".equalsIgnoreCase(this.role);
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    public String getPassword() {
        return null;
    }

    public String getUsername() {
        return this.email != null ? this.email : this.userId;
    }

    public boolean isAccountNonExpired() {
        return true;
    }

    public boolean isAccountNonLocked() {
        return true;
    }

    public boolean isCredentialsNonExpired() {
        return true;
    }

    public boolean isEnabled() {
        return true;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        UserPrincipal that = (UserPrincipal)o;
        return Objects.equals(this.userId, that.userId);
    }

    public int hashCode() {
        return Objects.hash(this.userId);
    }

    public String toString() {
        return "UserPrincipal{userId='" + this.userId + "', email='" + this.email + "', role='" + this.role + "'}";
    }
}

