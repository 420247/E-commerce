package com.shop.ecommerce.service;

import com.shop.ecommerce.dto.AuthResponse;
import com.shop.ecommerce.dto.LoginRequest;
import com.shop.ecommerce.dto.RegisterRequest;
import com.shop.ecommerce.model.Role;
import com.shop.ecommerce.model.User;
import com.shop.ecommerce.repository.UserRepository;
import com.shop.ecommerce.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService.
 *
 * We use Mockito to replace real dependencies (UserRepository, PasswordEncoder, etc.)
 * with test doubles — this isolates the service logic from the database and other
 * infrastructure concerns.
 *
 * @ExtendWith(MockitoExtension.class) — activates Mockito annotations (@Mock, @InjectMocks)
 * @Mock — creates a mock (fake implementation that records calls and returns configured values)
 * @InjectMocks — creates the class under test and injects mocks into its constructor
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest validRegisterRequest;
    private LoginRequest validLoginRequest;
    private User savedUser;

    /**
     * @BeforeEach runs before every test method.
     * Initialises shared test data so each test starts in a known state.
     */
    @BeforeEach
    void setUp() {
        validRegisterRequest = new RegisterRequest("John Doe", "john@example.com", "password123");
        validLoginRequest    = new LoginRequest("john@example.com", "password123");

        savedUser = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .password("$2a$10$hashedpassword")
                .role(Role.USER)
                .build();
    }

    // ── Registration tests ──────────────────────────────────────────────────

    @Test
    @DisplayName("register — success — returns access and refresh tokens")
    void register_success_returnsTokens() {
        // Arrange: email is available, encoder returns a hash, JwtService returns tokens
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashedpassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");

        // Act
        AuthResponse response = authService.register(validRegisterRequest);

        // Assert: both tokens are present and the user was saved exactly once
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("register — duplicate email — throws IllegalArgumentException")
    void register_duplicateEmail_throwsException() {
        // Arrange: email already exists in the database
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        // Act & Assert: expect the service to reject the duplicate
        assertThatThrownBy(() -> authService.register(validRegisterRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already in use");

        // The user must NOT be persisted if the email is taken
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("register — password is hashed — plain text never stored")
    void register_passwordIsHashed_plainTextNeverStored() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashedpassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            // The password saved to the database must NOT be the plain text value
            assertThat(user.getPassword()).isNotEqualTo("password123");
            assertThat(user.getPassword()).startsWith("$2a$");
            return user;
        });
        when(jwtService.generateToken(any())).thenReturn("token");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh");

        authService.register(validRegisterRequest);
    }

    @Test
    @DisplayName("register — new user always gets USER role")
    void register_newUser_alwaysGetsUserRole() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            // Self-registration must never produce ADMIN accounts
            assertThat(user.getRole()).isEqualTo(Role.USER);
            return user;
        });
        when(jwtService.generateToken(any())).thenReturn("token");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh");

        authService.register(validRegisterRequest);
    }

    // ── Login tests ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("login — valid credentials — returns tokens")
    void login_validCredentials_returnsTokens() {
        // AuthenticationManager.authenticate does not throw = credentials are correct
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(savedUser));
        when(jwtService.generateToken(savedUser)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(savedUser)).thenReturn("refresh-token");

        AuthResponse response = authService.login(validLoginRequest);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
    }

    @Test
    @DisplayName("login — wrong password — throws BadCredentialsException")
    void login_wrongPassword_throwsException() {
        // AuthenticationManager throws BadCredentialsException for wrong credentials
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(validLoginRequest))
                .isInstanceOf(BadCredentialsException.class);

        // No tokens should be generated if authentication failed
        verifyNoInteractions(jwtService);
    }
}
