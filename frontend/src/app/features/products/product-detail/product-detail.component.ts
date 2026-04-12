import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  OnInit,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { DecimalPipe } from '@angular/common';
import { ProductService } from '../../../core/services/product.service';
import { WishlistService } from '../../../core/services/wishlist.service';
import { AuthService } from '../../../core/services/auth.service';
import { Product } from '../../../shared/models/product.model';

/** Product detail page. Reads :id from the route and fetches the product. */
@Component({
  selector: 'app-product-detail',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    RouterLink,
    DecimalPipe,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
  ],
  templateUrl: './product-detail.component.html',
  styleUrl: './product-detail.component.scss',
})
export class ProductDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private productService = inject(ProductService);
  private snackBar = inject(MatSnackBar);
  wishlistService = inject(WishlistService);
  authService = inject(AuthService);
  private destroyRef = inject(DestroyRef);

  product = signal<Product | null>(null);
  isLoading = signal(true);

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.router.navigate(['/products']);
      return;
    }
    this.loadProduct(id);
  }

  loadProduct(id: string) {
    this.isLoading.set(true);
    this.productService
      .getProductById(+id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (product) => {
          this.product.set(product);
          this.isLoading.set(false);
        },
        error: () => {
          this.isLoading.set(false);
          this.snackBar.open('Product not found.', 'Close', { duration: 3000 });
          this.router.navigate(['/products']);
        },
      });
  }

  toggleWishlist() {
    const current = this.product();
    if (!current) return;

    if (!this.authService.isLoggedIn()) {
      const ref = this.snackBar.open('Please log in to save products.', 'Login', {
        duration: 3000,
      });
      ref
        .onAction()
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe(() => this.router.navigate(['/auth/login']));
      return;
    }

    if (this.wishlistService.isInWishlist(current.id)) {
      this.wishlistService
        .removeFromWishlist(current.id)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => this.snackBar.open('Removed from wishlist.', '', { duration: 2000 }),
        });
    } else {
      this.wishlistService
        .addToWishlist(current.id)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => this.snackBar.open('Added to wishlist!', '', { duration: 2000 }),
        });
    }
  }
}
