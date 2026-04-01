import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
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

/**
 * Product detail page — displays full information for a single product.
 * The product ID is extracted from the URL via ActivatedRoute.
 * e.g. /products/5 → id = 5
 */
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
    MatSnackBarModule,],
  templateUrl: './product-detail.component.html',
  styleUrl: './product-detail.component.scss'
})
export class ProductDetailComponent implements OnInit {
  product: Product | null = null;
  isLoading = true;

  constructor(
    private route: ActivatedRoute,// provides access to the current route's params
    private router: Router,
    private productService: ProductService,
    private snackBar: MatSnackBar,
    public wishlistService: WishlistService,
    public authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    // snapshot.paramMap gives the route params at the time of navigation
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.router.navigate(['/products']);
      return;
    }
    this.loadProduct(id);
  }

  loadProduct(id: string) {
    this.isLoading = true;
    this.productService.getProductById(+id).subscribe({
      next: (product) => {
        this.product = product;
        this.isLoading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.isLoading = false;
        this.cdr.markForCheck();
        this.snackBar.open('Product not found.', 'Close', { duration: 3000 });
        this.router.navigate(['/products']);
      }
    });
  }
  toggleWishlist() {
    if (!this.product) return;

    if (!this.authService.isLoggedIn()) {
      const ref = this.snackBar.open('Please log in to save products.', 'Login', { duration: 3000 });
      ref.onAction().subscribe(() => this.router.navigate(['/auth/login']));
      return;
    }

    if (this.wishlistService.isInWishlist(this.product.id)) {
      this.wishlistService.removeFromWishlist(this.product.id).subscribe({
        next: () => this.snackBar.open('Removed from wishlist.', '', { duration: 2000 })
      });
    } else {
      this.wishlistService.addToWishlist(this.product.id).subscribe({
        next: () => this.snackBar.open('Added to wishlist!', '', { duration: 2000 })
      });
    }
  }
}
