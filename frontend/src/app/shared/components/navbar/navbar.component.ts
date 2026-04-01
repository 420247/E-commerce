import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatBadgeModule } from '@angular/material/badge';
import { AuthService } from '../../../core/services/auth.service';
import { WishlistService } from '../../../core/services/wishlist.service';

/**
 * Application navigation bar.
 * Displays different actions based on authentication state.
 * The wishlist badge count is derived reactively from the wishlist signal.
 *
 * computed() — derives a new signal from existing signals.
 * Automatically recalculates when wishlistItems signal changes.
 */
@Component({
  selector: 'app-navbar',
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
  imports: [
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatBadgeModule,
    RouterLink,
    RouterLinkActive,
  ],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.scss',
})
export class NavbarComponent {
  authService = inject(AuthService);
  private wishlistService = inject(WishlistService);
  private router = inject(Router);

  /** Number of items in the wishlist — updates automatically when wishlist changes. */
  wishlistCount = computed(() => this.wishlistService.wishlistItems().length);

  /** Inserted by Angular inject() migration for backwards compatibility */
  constructor(...args: unknown[]);

  constructor() {}

  logout() {
    this.authService.logout();
  }
}
