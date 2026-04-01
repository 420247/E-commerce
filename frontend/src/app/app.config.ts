import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { routes } from './app.routes';
import { jwtInterceptor } from './core/interceptors/jwt.interceptor';

/**
 * Root application configuration (Angular 17+ standalone API).
 * Replaces the traditional AppModule — all providers are registered here.
 *
 * provideBrowserGlobalErrorListeners — registers global browser error handlers
 *   (window.onerror, window.onunhandledrejection) so unhandled errors are caught
 *   by Angular's ErrorHandler. Added by default in Angular 19+.
 *
 * provideRouter      — sets up client-side routing using routes in app.routes.ts
 * provideHttpClient  — registers Angular's HTTP client;
 *   withInterceptors attaches the JWT interceptor so every outgoing request
 *   automatically carries the Authorization header
 *
 */
export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([jwtInterceptor])),
  ],
};
