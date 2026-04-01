package com.shop.ecommerce.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT Authentication Filter — intercepts every HTTP request. Checks if the request has a valid JWT
 * token in the Authorization header. If valid, sets the user as authenticated in Spring Security
 * context.
 *
 * <p>Extends OncePerRequestFilter to guarantee it runs exactly once per request.
 */
@Component
@RequiredArgsConstructor // Lombok: generates constructor for all final fields
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    // Extract the Authorization header from the request
    // Expected format: "Bearer eyJhbGciOiJIUzI1NiJ9..."
    final String authHeader = request.getHeader("Authorization");

    // If no Authorization header or doesn't start with "Bearer ", skip this filter
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    // Extract the token by removing "Bearer " prefix (7 characters)
    final String jwt = authHeader.substring(7);

    // Extract username (email) from the token
    final String userEmail = jwtService.extractUsername(jwt);

    // If we have an email AND user is not already authenticated
    if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

      // Load user details from database
      UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

      // Validate the token against the user details
      if (jwtService.isTokenValid(jwt, userDetails)) {

        // Create authentication token — this is what Spring Security needs
        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(
                userDetails,
                null, // credentials are null — we use JWT, not password here
                userDetails.getAuthorities());

        // Attach request details (IP address, session, etc.)
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        // Set the authentication in the security context
        // From this point, Spring Security knows the user is authenticated
        SecurityContextHolder.getContext().setAuthentication(authToken);
      }
    }

    // Continue the filter chain — pass request to the next filter or controller
    filterChain.doFilter(request, response);
  }
}
