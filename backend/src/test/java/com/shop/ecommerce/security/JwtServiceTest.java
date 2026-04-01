package com.shop.ecommerce.security;

import com.shop.ecommerce.model.Role;
import com.shop.ecommerce.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for JwtService.
 *
 * We use ReflectionTestUtils to inject @Value fields directly without
 * starting the Spring context — keeps tests fast (no ApplicationContext).
 *
 * The secret key below is safe for tests only — never use in production.
 */
class JwtServiceTest {

    private JwtService jwtService;
    private User testUser;

    // Valid Base64-encoded 256-bit key — safe for tests only
    private static final String TEST_SECRET =
            "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        // Inject @Value fields using reflection (no Spring context needed)
        ReflectionTestUtils.setField(jwtService, "secretKey", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration",     900_000L);   // 15 min
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", 604_800_000L); // 7 days

        testUser = User.builder()
                .id(1L)
                .email("alice@example.com")
                .name("Alice")
                .password("hashed")
                .role(Role.USER)
                .build();
    }

    // ── Token generation tests ──────────────────────────────────────────────

    @Test
    @DisplayName("generateToken — returns non-null JWT string")
    void generateToken_returnsNonNullString() {
        String token = jwtService.generateToken(testUser);

        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    @DisplayName("generateToken — JWT has three parts separated by dots")
    void generateToken_hasThreeParts() {
        // A valid JWT always has the format: header.payload.signature
        String token = jwtService.generateToken(testUser);
        String[] parts = token.split("\\.");

        assertThat(parts).hasSize(3);
    }

    // ── Username extraction tests ───────────────────────────────────────────

    @Test
    @DisplayName("extractUsername — returns user email from token subject")
    void extractUsername_returnsEmail() {
        String token = jwtService.generateToken(testUser);

        String extracted = jwtService.extractUsername(token);

        // Spring Security uses email as the username in this app
        assertThat(extracted).isEqualTo("alice@example.com");
    }

    // ── Token validation tests ──────────────────────────────────────────────

    @Test
    @DisplayName("isTokenValid — own token — returns true")
    void isTokenValid_ownToken_returnsTrue() {
        String token = jwtService.generateToken(testUser);

        assertThat(jwtService.isTokenValid(token, testUser)).isTrue();
    }

    @Test
    @DisplayName("isTokenValid — token for different user — returns false")
    void isTokenValid_differentUser_returnsFalse() {
        User otherUser = User.builder()
                .id(2L).email("bob@example.com")
                .name("Bob").password("hashed").role(Role.USER).build();

        // Token was generated for alice, but we validate against bob
        String aliceToken = jwtService.generateToken(testUser);

        assertThat(jwtService.isTokenValid(aliceToken, otherUser)).isFalse();
    }

    @Test
    @DisplayName("isTokenValid — expired token — returns false")
    void isTokenValid_expiredToken_returnsFalse() {
        // Override expiration to -1 ms so the token is already expired when created
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1L);

        String expiredToken = jwtService.generateToken(testUser);

        assertThat(jwtService.isTokenValid(expiredToken, testUser)).isFalse();
    }

    // ── Refresh token tests ─────────────────────────────────────────────────

    @Test
    @DisplayName("generateRefreshToken — valid token — contains correct subject")
    void generateRefreshToken_containsCorrectEmail() {
        String refreshToken = jwtService.generateRefreshToken(testUser);

        assertThat(jwtService.extractUsername(refreshToken))
                .isEqualTo("alice@example.com");
    }

    @Test
    @DisplayName("access token and refresh token — are different strings")
    void accessAndRefreshTokens_areDifferent() {
        String access  = jwtService.generateToken(testUser);
        String refresh = jwtService.generateRefreshToken(testUser);

        // Different expiry = different signature = different token strings
        assertThat(access).isNotEqualTo(refresh);
    }
}
