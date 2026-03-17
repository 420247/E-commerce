import { Product } from './product.model';
import { User } from './user.model';

/**
 * Represents a single entry in a user's wishlist.
 * Matches the WishlistItem entity returned by GET /api/wishlist.
 *
 * The backend stores wishlist items as a join between users and products,
 * with a UNIQUE(user_id, product_id) constraint to prevent duplicates.
 */
export interface WishlistItem {
  id: number;
  /** The user who saved this product. */
  user: User;
  /** The saved product. */
  product: Product;
  /** ISO timestamp of when the item was added. */
  addedAt: string;
}
