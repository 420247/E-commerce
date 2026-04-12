import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  OnInit,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { DecimalPipe, DatePipe } from '@angular/common';
import { WishlistService } from '../../../core/services/wishlist.service';
import { WishlistItem } from '../../../shared/models/wishlist.model';

/** Wishlist page — shows everything the signed-in user has saved. Guarded by authGuard. */
@Component({
  selector: 'app-wishlist-page',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    RouterLink,
    DecimalPipe,
    DatePipe,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
  ],
  templateUrl: './wishlist-page.component.html',
  styleUrl: './wishlist-page.component.scss',
})
export class WishlistPageComponent implements OnInit {
  wishlistService = inject(WishlistService);
  private snackBar = inject(MatSnackBar);
  private destroyRef = inject(DestroyRef);

  isLoading = signal(true);

  ngOnInit() {
    this.wishlistService
      .loadWishlist()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => this.isLoading.set(false),
        error: () => {
          this.isLoading.set(false);
          this.snackBar.open('Failed to load wishlist', 'Close', { duration: 3000 });
        },
      });
  }

  removeItem(item: WishlistItem) {
    this.wishlistService
      .removeFromWishlist(item.product.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => this.snackBar.open('Removed from wishlist.', '', { duration: 2000 }),
      });
  }
}
