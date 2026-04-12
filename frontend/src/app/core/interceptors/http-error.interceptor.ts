import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

/**
 * Turns HTTP failures into user-facing snackbars in one place.
 *
 * - 401 outside /auth/** = session expired, force logout + redirect.
 * - 4xx on /auth/** is left to the login/register forms for inline errors.
 * - The error is always rethrown so component-level callbacks still run.
 */
export const httpErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const snackBar = inject(MatSnackBar);
  const router = inject(Router);
  const authService = inject(AuthService);

  const isAuthEndpoint = req.url.includes('/auth/');

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 0) {
        // Network down or server unreachable
        snackBar.open('Unable to reach the server. Check your connection.', 'Close', {
          duration: 4000,
        });
      } else if (error.status === 401 && !isAuthEndpoint) {
        // Token expired — force logout
        authService.logout();
        snackBar.open('Session expired. Please log in again.', 'Close', { duration: 4000 });
        router.navigate(['/auth/login']);
      } else if (error.status === 403) {
        snackBar.open("You don't have permission to do that.", 'Close', { duration: 4000 });
      } else if (error.status === 429) {
        snackBar.open('Too many requests. Please slow down and try again.', 'Close', {
          duration: 4000,
        });
      } else if (error.status >= 500) {
        snackBar.open('Server error. Please try again later.', 'Close', { duration: 4000 });
      } else if (error.status >= 400 && !isAuthEndpoint) {
        const message =
          error.error && typeof error.error === 'object' && 'message' in error.error
            ? (error.error as { message?: string }).message
            : null;
        snackBar.open(message ?? 'Request failed. Please try again.', 'Close', { duration: 4000 });
      }

      return throwError(() => error);
    }),
  );
};
