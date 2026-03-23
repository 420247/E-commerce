import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { DecimalPipe, DatePipe } from '@angular/common';
import { WishlistService } from '../../../core/services/wishlist.service';
import { WishlistItem } from '../../../shared/models/wishlist.model';

/**
 * Wishlist page — displays all products saved by the authenticated user.
 * Protected by authGuard — only accessible when logged in.
 * Loads wishlist data from the backend on init.
 */

@Component({
  selector: 'app-wishlist-page',
  standalone: true,
  changeDetection : ChangeDetectionStrategy.OnPush,
  imports: [
    RouterLink,
    DecimalPipe,
    DatePipe,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  templateUrl: './wishlist-page.component.html',
  styleUrl: './wishlist-page.component.scss'
})
export class WishlistPageComponent implements OnInit {
  isLoading = true;

  constructor(
    public wishlistService: WishlistService,
    private snackBar: MatSnackBar,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.wishlistService.loadWishlist().subscribe({
      next: () => {
        this.isLoading = false;
        this.cdr.markForCheck(); // since we're using OnPush, we need to manually trigger change detection after async data loads
      },
      error: (err) => {
        this.isLoading = false;
        this.cdr.markForCheck();
        this.snackBar.open('Failed to load wishlist', 'Close', { duration: 3000 });
      }
    });
  }
  removeItem(item: WishlistItem) {
    this.wishlistService.removeFromWishlist(item.product.id).subscribe({
      next: () => {
        this.snackBar.open('Removed from wishlist.', '', { duration: 2000 });
        this.cdr.markForCheck();
      }
    });
  }
}
