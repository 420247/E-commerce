import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs/operators';
import { WishlistItem } from '../../shared/models/wishlist.model';
import { environment } from '../../../environments/environment.prod';

/**
 * Manages the user's wishlist state and communicates with the backend API.
 *
 * The wishlist is stored in a reactive signal so any component reading
 * `wishlistItems()` automatically re-renders when items are added or removed.
 * The signal is the single source of truth — no separate state management library needed.
 */
@Injectable({ providedIn: 'root' })
export class WishlistService {
  private http = inject(HttpClient);

  /**
   * Reactive signal holding the current user's wishlist items.
   * Updated optimistically on add/remove without a full reload.
   */
  wishlistItems = signal<WishlistItem[]>([]);

  private apiUrl = `${environment.apiUrl}/wishlist`;

  constructor() {}

  /**
   * Loads the user's wishlist from GET /api/wishlist and stores it in the signal.
   * Should be called once after the user logs in.
   */
  loadWishlist() {
    return this.http
      .get<WishlistItem[]>(this.apiUrl)
      .pipe(tap((items) => this.wishlistItems.set(items)));
  }

  /**
   * Adds a product to the wishlist via POST /api/wishlist/:productId.
   * Appends the new item to the signal immediately on success.
   */
  addToWishlist(productId: number) {
    return this.http
      .post<WishlistItem>(`${this.apiUrl}/${productId}`, {})
      .pipe(tap((item) => this.wishlistItems.update((items) => [...items, item])));
  }

  /**
   * Removes a product from the wishlist via DELETE /api/wishlist/:productId.
   * Filters the item out of the signal immediately on success.
   */
  removeFromWishlist(productId: number) {
    return this.http
      .delete(`${this.apiUrl}/${productId}`)
      .pipe(
        tap(() =>
          this.wishlistItems.update((items) => items.filter((i) => i.product.id !== productId)),
        ),
      );
  }

  /** Returns true if the given product is already in the wishlist. */
  isInWishlist(productId: number): boolean {
    return this.wishlistItems().some((i) => i.product.id === productId);
  }
}
