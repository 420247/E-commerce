package com.shop.ecommerce.service;

import com.shop.ecommerce.dto.AuthResponse;
import com.shop.ecommerce.dto.LoginRequest;
import com.shop.ecommerce.dto.RegisterRequest;
import com.shop.ecommerce.model.Role;
import com.shop.ecommerce.model.User;
import com.shop.ecommerce.repository.UserRepository;
import com.shop.ecommerce.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service handling user registration and login logic.
 * Generates JWT tokens after successful authentication.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Registers a new user in the system.
     * Hashes the password and saves user to database.
     * Returns JWT tokens so user is immediately logged in after registration.
     *
     * @throws IllegalArgumentException if email is already taken
     */
    public AuthResponse register(RegisterRequest request) {

        // Check if email is already registered
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already in use: " + request.email());
        }

        // Build the user entity — password is hashed with BCrypt
        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password())) // never store plain text
                .role(Role.USER) // all new users get USER role by default
                .build();

        // Save user to database
        userRepository.save(user);

        // Generate tokens for the newly registered user
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken);
    }

    /**
     * Authenticates an existing user.
     * Spring Security verifies email and password automatically.
     * Throws AuthenticationException if credentials are invalid.
     */
    public AuthResponse login(LoginRequest request) {

        // AuthenticationManager verifies credentials against the database
        // Throws BadCredentialsException if email/password don't match
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        // If we reach here, credentials are valid — load the user
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Generate fresh tokens
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken);
    }
}
