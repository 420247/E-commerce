import { ErrorHandler, Injectable, NgZone, inject } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { MatSnackBar } from '@angular/material/snack-bar';

/**
 * Last-resort handler for errors Angular would otherwise silently swallow.
 * HTTP errors are already handled by httpErrorInterceptor — skipped here
 * to avoid duplicate snackbars.
 * NgZone.run() is needed because errors thrown outside Angular's zone
 * (e.g. from setTimeout) won't trigger change detection on the snackbar.
 */
@Injectable()
export class GlobalErrorHandler implements ErrorHandler {
  private snackBar = inject(MatSnackBar);
  private zone = inject(NgZone);

  handleError(error: unknown): void {
    console.error('[GlobalErrorHandler]', error);

    if (error instanceof HttpErrorResponse) {
      return;
    }

    this.zone.run(() => {
      this.snackBar.open('An unexpected error occurred.', 'Close', { duration: 4000 });
    });
  }
}
