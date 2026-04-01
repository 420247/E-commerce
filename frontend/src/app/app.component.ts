import { Component, OnInit, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from './shared/components/navbar/navbar.component';
import { AuthService } from './core/services/auth.service';
import { WishlistService } from './core/services/wishlist.service';

/**
 * Root component — the application shell.
 * Renders the navbar and the current route via <router-outlet>.
 * On init, loads the wishlist if the user is already logged in (session restored from token).
 */
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, NavbarComponent],
  template: `
    <app-navbar />
    <main class="main-content">
      <router-outlet />
    </main>
  `,
  styles: [
    `
      .main-content {
        max-width: 1200px;
        margin: 0 auto;
        padding: 24px 16px;
      }
    `,
  ],
})
export class AppComponent implements OnInit {
  private authService = inject(AuthService);
  private wishlistService = inject(WishlistService);

  /** Inserted by Angular inject() migration for backwards compatibility */
  constructor(...args: unknown[]);

  constructor() {}

  ngOnInit() {
    // If user has a valid token from a previous session, load their wishlist immediately
    if (this.authService.isLoggedIn()) {
      this.wishlistService.loadWishlist().subscribe();
    }
  }
}
