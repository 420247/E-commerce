package com.shop.ecommerce.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * User entity mapped to the "users" table in the database.
 * Implements UserDetails to integrate with Spring Security authentication.
 */
@Data               // Lombok: generates getters, setters, toString, equals, hashCode
@Builder            // Lombok: enables builder pattern e.g. User.builder().email("...").build()
@NoArgsConstructor  // Lombok: generates no-args constructor (required by JPA)
@AllArgsConstructor // Lombok: generates constructor with all fields
@Entity             // JPA: marks this class as a database entity
@Table(name = "users") // JPA: maps to the "users" table
public class User implements UserDetails {

    @Id // Primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment
    private Long id;

    @Column(nullable = false, unique = true) // Email must be unique
    private String email;

    @Column(nullable = false) // Stored as bcrypt hash, never plain text
    private String password;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING) // Store role as string e.g. "USER", not 0/1
    private Role role;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Returns the authorities (roles) granted to the user.
     * Spring Security uses this to check permissions.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    /**
     * Spring Security uses email as the username for authentication.
     */
    @Override
    public String getUsername() {
        return email;
    }

    // Account status checks — all return true for simplicity
    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

    /**
     * Automatically sets createdAt before the entity is first saved to the database.
     */
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
