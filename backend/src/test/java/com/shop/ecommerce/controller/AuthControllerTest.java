package com.shop.ecommerce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.ecommerce.dto.AuthResponse;
import com.shop.ecommerce.dto.LoginRequest;
import com.shop.ecommerce.dto.RegisterRequest;
import com.shop.ecommerce.repository.UserRepository;
import com.shop.ecommerce.security.JwtService;
import com.shop.ecommerce.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController.
 *
 * @WebMvcTest spins up only the web layer (controllers, filters, security config)
 * without starting a full ApplicationContext or connecting to a database.
 * This makes the tests fast while still verifying HTTP behaviour.
 *
 * MockMvc lets us send HTTP requests and assert on the response
 * without starting a real server.
 */
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Replace the real AuthService with a mock so we control its behaviour
    @MockBean
    private AuthService authService;

    // SecurityConfig requires UserRepository to build UserDetailsService
    @MockBean
    private UserRepository userRepository;

    // JwtAuthenticationFilter (loaded by SecurityConfig) requires JwtService
    @MockBean
    private JwtService jwtService;

    // ── POST /api/auth/register ─────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/auth/register — valid body — returns 200 with tokens")
    @WithMockUser
    void register_validRequest_returns200() throws Exception {
        RegisterRequest request = new RegisterRequest("Alice", "alice@example.com", "password123");
        AuthResponse    response = new AuthResponse("access-token", "refresh-token");

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    @DisplayName("POST /api/auth/register — missing email — returns 400")
    @WithMockUser
    void register_missingEmail_returns400() throws Exception {
        // @NotBlank validation on RegisterRequest.email should trigger
        RegisterRequest invalidRequest = new RegisterRequest("Alice", "", "password123");

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/register — password too short — returns 400")
    @WithMockUser
    void register_shortPassword_returns400() throws Exception {
        // @Size(min = 6) on RegisterRequest.password should trigger
        RegisterRequest invalidRequest = new RegisterRequest("Alice", "alice@example.com", "123");

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    // ── POST /api/auth/login ────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/auth/login — valid credentials — returns 200 with tokens")
    @WithMockUser
    void login_validCredentials_returns200() throws Exception {
        LoginRequest request  = new LoginRequest("alice@example.com", "password123");
        AuthResponse response = new AuthResponse("access-token", "refresh-token");

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"));
    }

    @Test
    @DisplayName("POST /api/auth/login — invalid email format — returns 400")
    @WithMockUser
    void login_invalidEmailFormat_returns400() throws Exception {
        // @Email validation on LoginRequest.email should trigger
        LoginRequest invalidRequest = new LoginRequest("not-an-email", "password123");

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
