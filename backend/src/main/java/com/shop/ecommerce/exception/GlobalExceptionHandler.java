package com.shop.ecommerce.exception;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centralised exception handler. Keeps error responses consistent and stops Spring Boot from
 * leaking stack traces on 500s. Full details are logged server-side; clients get a minimal JSON
 * body: {status, error, message}.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * @Valid failures — 400 with per-field messages.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
    Map<String, String> fieldErrors = new HashMap<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(err -> fieldErrors.put(err.getField(), err.getDefaultMessage()));

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("status", HttpStatus.BAD_REQUEST.value());
    body.put("error", "Validation failed");
    body.put("message", "Request contains invalid fields");
    body.put("fields", fieldErrors);
    return ResponseEntity.badRequest().body(body);
  }

  /** Thrown by services for bad input — 400 with the exception message. */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
    log.warn("Illegal argument: {}", ex.getMessage());
    return build(HttpStatus.BAD_REQUEST, "Bad request", ex.getMessage());
  }

  /**
   * Any auth failure — 401 with a generic message. Never reveal which half (email or password) was
   * wrong, as that helps attackers enumerate valid accounts.
   */
  @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
  public ResponseEntity<Map<String, Object>> handleAuth(AuthenticationException ex) {
    log.warn("Authentication failed: {}", ex.getMessage());
    return build(HttpStatus.UNAUTHORIZED, "Unauthorized", "Invalid email or password");
  }

  /** Authenticated but not authorised — 403. */
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
    log.warn("Access denied: {}", ex.getMessage());
    return build(HttpStatus.FORBIDDEN, "Forbidden", "You are not allowed to do that");
  }

  /** Entity not found — 404. No internal identifiers are included in the response. */
  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<Map<String, Object>> handleNotFound(NoSuchElementException ex) {
    log.warn("Not found: {}", ex.getMessage());
    return build(HttpStatus.NOT_FOUND, "Not found", "Resource not found");
  }

  /** Catch-all — log the full stack trace server-side, return a generic 500 to the client. */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
    log.error("Unhandled exception", ex);
    return build(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Internal server error",
        "An unexpected error occurred. Please try again later.");
  }

  private ResponseEntity<Map<String, Object>> build(
      HttpStatus status, String error, String message) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("status", status.value());
    body.put("error", error);
    body.put("message", message);
    return ResponseEntity.status(status).body(body);
  }
}
