import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { AuthResponse, LoginRequest, RegisterRequest, User } from '../../shared/models/user.model';
import { environment } from '../../../environments/environment';

/**
 * Handles all authentication logic: registration, login, logout, and session restoration.
 *
 * Tokens are stored in localStorage so the session persists across browser refreshes.
 * On app start, the service reads the stored token and restores the user state automatically.
 *
 * `providedIn: 'root'` — Angular creates a single instance shared across the whole app (singleton).
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  /**
   * Reactive signal holding the currently authenticated user.
   * Components that read `currentUser()` automatically re-render when it changes.
   * Null means no user is logged in.
   */
  currentUser = signal<User | null>(null);

  private apiUrl = `${environment.apiUrl}/auth`;

  /** Inserted by Angular inject() migration for backwards compatibility */
  constructor(...args: unknown[]);

  constructor() {
    this.loadUserFromToken();
  }

  /**
   * Registers a new user account and saves the returned JWT tokens.
   * Returns an Observable — must be subscribed to in the component.
   */
  register(request: RegisterRequest) {
    return this.http
      .post<AuthResponse>(`${this.apiUrl}/register`, request)
      .pipe(tap((response) => this.saveTokens(response)));
  }

  /**
   * Authenticates an existing user and saves the returned JWT tokens.
   * Returns an Observable — must be subscribed to in the component.
   */
  login(request: LoginRequest) {
    return this.http
      .post<AuthResponse>(`${this.apiUrl}/login`, request)
      .pipe(tap((response) => this.saveTokens(response)));
  }

  /** Clears tokens from storage, resets user state, and redirects to the login page. */
  logout() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    this.currentUser.set(null);
    this.router.navigate(['/auth/login']);
  }

  /** Returns the stored access token, or null if the user is not logged in. */
  getAccessToken(): string | null {
    return localStorage.getItem('accessToken');
  }

  /** Returns true if a valid access token exists in storage. */
  isLoggedIn(): boolean {
    return !!this.getAccessToken();
  }

  /**
   * Decodes the JWT payload to restore user state from the stored token.
   * A JWT has three Base64-encoded parts separated by dots: header.payload.signature.
   * The payload contains the user's email (sub) and expiry (exp).
   * No backend call is needed — all information is embedded in the token.
   */
  private loadUserFromToken() {
    const token = this.getAccessToken();
    if (token) {
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        this.currentUser.set({ id: 0, email: payload.sub, name: '', role: 'USER' });
      } catch {
        this.logout();
      }
    }
  }

  /** Persists tokens to localStorage and restores user state from the new access token. */
  private saveTokens(response: AuthResponse) {
    localStorage.setItem('accessToken', response.accessToken);
    localStorage.setItem('refreshToken', response.refreshToken);
    this.loadUserFromToken();
  }
}
