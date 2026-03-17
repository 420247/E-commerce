import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Route guard that protects pages requiring authentication.
 * If the user is not logged in, they are redirected to the login page.
 *
 * Functional guard syntax (Angular 17+) — registered directly in the route config:
 * `{ path: 'wishlist', canActivate: [authGuard], component: WishlistComponent }`
 *
 * CanActivateFn returns true to allow navigation or false to block it.
 * Redirecting and returning false prevents the target route from loading.
 */
export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isLoggedIn()) {
    return true;
  }

  // Store the attempted URL to redirect back after login (optional enhancement)
  router.navigate(['/auth/login']);
  return false;
};
