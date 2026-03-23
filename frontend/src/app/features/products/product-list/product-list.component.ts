import { Component, OnInit, ChangeDetectorRef, ChangeDetectionStrategy} from '@angular/core';
import { FormsModule, FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { TitleCasePipe, DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ProductService } from '../../../core/services/product.service';
import { WishlistService } from '../../../core/services/wishlist.service';
import { AuthService } from '../../../core/services/auth.service';
import { Product, ProductFilter } from '../../../shared/models/product.model';

/**
 * Product listing page with filtering and AI search.
 *
 * OnInit — lifecycle hook called once after Angular initialises the component.
 * Used here to load the initial product list from the backend.
 *
 * MatSnackBar — Angular Material service for brief notification messages
 * shown at the bottom of the screen (e.g. "Added to wishlist").
 */
@Component({
  selector: 'app-product-list',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
imports: [
    ReactiveFormsModule,
    FormsModule,
    TitleCasePipe,
    DecimalPipe,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  templateUrl: './product-list.component.html',
  styleUrl: './product-list.component.scss'
})
export class ProductListComponent implements OnInit {

  products: Product[] = [];
  isLoading = false;
  isAiLoading = false;
  aiExplanation = '';

  filterForm: FormGroup;
  aiSearchQuery = '';

  /** Available product categories shown in the filter dropdown. */
  categories = ['electronics', 'clothing', 'books', 'sports', 'home'];

  constructor(
    private fb: FormBuilder,
    private productService: ProductService,
    public wishlistService: WishlistService,
    public authService: AuthService,
    private snackBar: MatSnackBar,
    private cdr: ChangeDetectorRef
  ) {
    this.filterForm = this.fb.group({
      category: [''],
      minPrice: [''],
      maxPrice: [''],
      minRating: ['']
    });
  }

  ngOnInit() {
    this.loadProducts();
  }

  /**
   * Loads products from the backend, applying only the filters that have values.
   * Empty strings are treated as "no filter" — not sent to the backend.
   */
  loadProducts() {
    this.isLoading = true;
    this.aiExplanation = '';

    const formValue = this.filterForm.value;
    const filter: ProductFilter = {};

    if (formValue.category) filter.category = formValue.category;
    if (formValue.minPrice) filter.minPrice = +formValue.minPrice; // + converts string to number
    if (formValue.maxPrice) filter.maxPrice = +formValue.maxPrice;
    if (formValue.minRating) filter.minRating = +formValue.minRating;

    this.productService.getProducts(filter).subscribe({
      next: (products) => {
        this.products = products;
        this.isLoading = false;
        this.cdr.markForCheck(); // since we're using OnPush, we need to manually trigger change detection after async data loads
      },
      error: () => {
        this.isLoading = false;
        this.cdr.markForCheck();
        this.snackBar.open('Failed to load products.', 'Close', { duration: 3000 });
      }
    });
  }

  /** Resets all filters and reloads the full product list. */
  clearFilters() {
    this.filterForm.reset();
    this.loadProducts();
  }

  /**
   * Sends the natural language query to the AI search endpoint.
   * Replaces the product list with AI-matched results and shows Claude's explanation.
   */
  aiSearch() {
    if (!this.aiSearchQuery.trim()) return;

    this.isAiLoading = true;
    this.productService.aiSearch(this.aiSearchQuery).subscribe({
      next: (response) => {
        this.products = response.products;
        this.aiExplanation = response.explanation;
        this.isAiLoading = false;
      },
      error: () => {
        this.isAiLoading = false;
        this.snackBar.open('AI search failed. Please try again.', 'Close', { duration: 3000 });
      }
    });
  }

  /**
   * Adds or removes a product from the wishlist depending on its current state.
   * Requires the user to be logged in — shows a message otherwise.
   */
  toggleWishlist(product: Product, event: Event) {
    event.preventDefault(); // prevent navigation to product detail
    event.stopPropagation();

    if (!this.authService.isLoggedIn()) {
      this.snackBar.open('Please log in to save products.', 'Login', { duration: 3000 });
      return;
    }

    if (this.wishlistService.isInWishlist(product.id)) {
      this.wishlistService.removeFromWishlist(product.id).subscribe({
        next: () => this.snackBar.open('Removed from wishlist.', '', { duration: 2000 })
      });
    } else {
      this.wishlistService.addToWishlist(product.id).subscribe({
        next: () => this.snackBar.open('Added to wishlist!', '', { duration: 2000 })
      });
    }
  }
}
