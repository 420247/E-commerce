import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

/**
 * HTTP interceptor that automatically attaches the JWT access token
 * to every outgoing request as an Authorization header.
 *
 * This means individual services never need to manually add the header —
 * authentication is handled transparently at the HTTP layer.
 *
 * Functional interceptor syntax (Angular 17+) — registered in app.config.ts
 * via provideHttpClient(withInterceptors([jwtInterceptor])).
 *
 * @example
 * // Without interceptor — every service would need this:
 * this.http.get('/api/wishlist', { headers: { Authorization: `Bearer ${token}` } });
 *
 * // With interceptor — the header is added automatically:
 * this.http.get('/api/wishlist');
 */
export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getAccessToken();

  if (token) {
    // HTTP requests are immutable in Angular — clone and modify, never mutate directly
    const authReq = req.clone({
      headers: req.headers.set('Authorization', `Bearer ${token}`)
    });
    return next(authReq);
  }

  // No token available — forward the original request unchanged (public endpoints)
  return next(req);
};