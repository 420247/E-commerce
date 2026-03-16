package com.shop.ecommerce.repository;

import com.shop.ecommerce.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for User entity.
 * JpaRepository provides basic CRUD operations out of the box.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Spring Data JPA automatically generates SQL from method name:
     * SELECT * FROM users WHERE email = ?
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user with given email already exists.
     * Used during registration to prevent duplicates.
     */
    boolean existsByEmail(String email);
}