/**
 * Represents an authenticated user in the system.
 * Matches the User entity returned by the backend after login.
 */
export interface User {
  id: number;
  email: string;
  name: string;
  /** USER has standard access; ADMIN has full store management access. */
  role: 'USER' | 'ADMIN';
}

/**
 * Response returned by POST /api/auth/login and POST /api/auth/register.
 * The accessToken is short-lived (15 min); the refreshToken is long-lived (7 days)
 * and is used to obtain a new accessToken without re-authentication.
 */
export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
}

/** Request body for POST /api/auth/login. */
export interface LoginRequest {
  email: string;
  password: string;
}

/** Request body for POST /api/auth/register. */
export interface RegisterRequest {
  name: string;
  email: string;
  /** Must be at least 6 characters. Hashed with BCrypt on the backend. */
  password: string;
}
