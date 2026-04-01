package com.shop.ecommerce.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service responsible for generating, parsing and validating JWT tokens.
 * All token logic is centralized here — no JWT code should exist outside this class.
 */
@Service
public class JwtService {

    // Injected from application.yml — application.security.jwt.secret-key
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    // Access token lifetime in milliseconds (15 minutes)
    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    // Refresh token lifetime in milliseconds (7 days)
    @Value("${application.security.jwt.refresh-expiration}")
    private long refreshExpiration;

    /**
     * Extracts the username (email) from a JWT token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts a specific claim from the token using a resolver function.
     * Generic method — can extract any field from the token payload.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Generates an access token for the given user.
     * The token contains the user's email as subject.
     */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /**
     * Generates an access token with additional custom claims.
     * Extra claims can include user role, id, etc.
     */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    /**
     * Generates a refresh token for the given user.
     * Refresh tokens have longer expiration and are used to get new access tokens.
     */
    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, refreshExpiration);
    }

    /**
     * Core method that builds and signs a JWT token.
     *
     * @param extraClaims  additional data to include in token payload
     * @param userDetails  the authenticated user
     * @param expiration   token lifetime in milliseconds
     */
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername()) // email is the subject
                .issuedAt(new Date(System.currentTimeMillis())) // token creation time
                .expiration(new Date(System.currentTimeMillis() + expiration)) // expiry time
                .signWith(getSigningKey()) // sign with our secret key
                .compact(); // build the final token string
    }

    /**
     * Validates a token — checks if it belongs to the user and hasn't expired.
     * Returns false (instead of throwing) if the token is expired or malformed.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * Checks if the token expiration date is in the past.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extracts the expiration date from the token.
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Parses and returns all claims (payload) from the token.
     * Uses the secret key to verify the token signature.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey()) // verify signature with our secret key
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Converts the secret key string to a cryptographic key object.
     * HMAC-SHA algorithm is used for signing.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
