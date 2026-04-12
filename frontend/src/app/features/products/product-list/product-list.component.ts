import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  OnInit,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule, FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { TitleCasePipe, DecimalPipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
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

/** Product listing page with filter form and AI search. */
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
    MatSnackBarModule,
  ],
  templateUrl: './product-list.component.html',
  styleUrl: './product-list.component.scss',
})
export class ProductListComponent implements OnInit {
  private fb = inject(FormBuilder);
  private productService = inject(ProductService);
  wishlistService = inject(WishlistService);
  authService = inject(AuthService);
  private snackBar = inject(MatSnackBar);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  products = signal<Product[]>([]);
  isLoading = signal(false);
  isAiLoading = signal(false);
  aiExplanation = signal('');

  filterForm: FormGroup;
  aiSearchQuery = '';

  categories = ['electronics', 'clothing', 'books', 'sports', 'home'];

  constructor() {
    this.filterForm = this.fb.group({
      category: [''],
      minPrice: [''],
      maxPrice: [''],
      minRating: [''],
    });
  }

  ngOnInit() {
    this.loadProducts();
  }

  /** Loads products, passing only the filter fields the user actually filled in. */
  loadProducts() {
    this.isLoading.set(true);
    this.aiExplanation.set('');

    const formValue = this.filterForm.value;
    const filter: ProductFilter = {};

    if (formValue.category) filter.category = formValue.category;
    if (formValue.minPrice) filter.minPrice = +formValue.minPrice;
    if (formValue.maxPrice) filter.maxPrice = +formValue.maxPrice;
    if (formValue.minRating) filter.minRating = +formValue.minRating;

    this.productService
      .getProducts(filter)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (products) => {
          this.products.set(products);
          this.isLoading.set(false);
        },
        error: () => {
          this.isLoading.set(false);
          this.snackBar.open('Failed to load products.', 'Close', { duration: 3000 });
        },
      });
  }

  clearFilters() {
    this.filterForm.reset();
    this.loadProducts();
  }

  /** Sends a natural-language query to the AI search endpoint. */
  aiSearch() {
    if (!this.aiSearchQuery.trim()) return;

    this.isAiLoading.set(true);
    this.productService
      .aiSearch(this.aiSearchQuery)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (response) => {
          this.products.set(response.products);
          this.aiExplanation.set(response.explanation);
          this.isAiLoading.set(false);
        },
        error: () => {
          this.isAiLoading.set(false);
          this.snackBar.open('AI search failed. Please try again.', 'Close', { duration: 3000 });
        },
      });
  }

  toggleWishlist(product: Product, event: Event) {
    // Card is wrapped in routerLink — stop the click from navigating
    event.preventDefault();
    event.stopPropagation();

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

    if (this.wishlistService.isInWishlist(product.id)) {
      this.wishlistService
        .removeFromWishlist(product.id)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => this.snackBar.open('Removed from wishlist.', '', { duration: 2000 }),
        });
    } else {
      this.wishlistService
        .addToWishlist(product.id)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: () => this.snackBar.open('Added to wishlist!', '', { duration: 2000 }),
        });
    }
  }
}
