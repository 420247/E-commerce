package com.shop.ecommerce.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Sliding-window rate limiter for POST /api/auth/login. Caps each client IP at 5 attempts per
 * minute — over-limit requests get HTTP 429. Move to Redis when running multiple instances.
 */
@Slf4j
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

  private static final int MAX_REQUESTS = 5;
  private static final Duration WINDOW = Duration.ofMinutes(1);
  private static final String LOGIN_PATH = "/api/auth/login";

  // ConcurrentHashMap — thread-safe, multiple requests can arrive simultaneously
  private final ConcurrentHashMap<String, Deque<Instant>> attempts = new ConcurrentHashMap<>();

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    if (isLoginRequest(request) && isRateLimited(clientKey(request))) {
      log.warn("Rate limit exceeded for {}", clientKey(request));
      response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response
          .getWriter()
          .write(
              "{\"status\":429,\"error\":\"Too Many Requests\","
                  + "\"message\":\"Too many login attempts. Please try again later.\"}");
      return;
    }

    filterChain.doFilter(request, response);
  }

  private boolean isLoginRequest(HttpServletRequest request) {
    return LOGIN_PATH.equals(request.getRequestURI())
        && "POST".equalsIgnoreCase(request.getMethod());
  }

  /** Evicts expired timestamps, records current attempt, returns true if limit exceeded. */
  private boolean isRateLimited(String key) {
    Deque<Instant> timestamps = attempts.computeIfAbsent(key, k -> new ArrayDeque<>());
    Instant now = Instant.now();
    Instant cutoff = now.minus(WINDOW);

    // synchronized on the individual queue, not the whole map — other IPs are not blocked
    synchronized (timestamps) {
      while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(cutoff)) {
        timestamps.pollFirst();
      }
      if (timestamps.size() >= MAX_REQUESTS) {
        return true;
      }
      timestamps.addLast(now);
      return false;
    }
  }

  /** Real client IP — honours X-Forwarded-For when behind a proxy/nginx. */
  private String clientKey(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) {
      return forwarded.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}
