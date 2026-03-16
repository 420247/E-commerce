package com.shop.ecommerce.dto;

/**
 * DTO returned after successful login or registration.
 * Contains both access token (short-lived) and refresh token (long-lived).
 */
public record AuthResponse(

        // Short-lived token used for API requests (15 minutes)
        String accessToken,

        // Long-lived token used to get a new access token (7 days)
        String refreshToken
) {}