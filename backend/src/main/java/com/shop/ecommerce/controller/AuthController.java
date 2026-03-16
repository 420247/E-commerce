package com.shop.ecommerce.controller;

import com.shop.ecommerce.dto.AuthResponse;
import com.shop.ecommerce.dto.LoginRequest;
import com.shop.ecommerce.dto.RegisterRequest;
import com.shop.ecommerce.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication endpoints.
 * Handles user registration and login — returns JWT tokens on success.
 * Base URL: /api/auth
 */
@RestController  // Marks class as REST controller — every method returns JSON automatically
@RequestMapping("/api/auth") // All endpoints here start with /api/auth
@RequiredArgsConstructor     // Lombok: generates constructor injection for all final fields
@CrossOrigin(origins = "http://localhost:4200") // Allows Angular dev server to call this API (CORS)
public class AuthController {

    private final AuthService authService; // Injected by Spring — handles all auth business logic

    /**
     * POST /api/auth/register
     * Creates a new user account and returns JWT tokens.
     *
     * @Valid       — runs validation from RegisterRequest (@NotBlank, @Email, @Size)
     * @RequestBody — parses JSON request body into RegisterRequest object
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authService.register(request)); // 200 OK + tokens
    }

    /**
     * POST /api/auth/login
     * Authenticates user by email/password and returns JWT tokens.
     * Returns 401 Unauthorized automatically if credentials are wrong.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(request)); // 200 OK + tokens
    }
}