import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../../core/services/auth.service';
import { WishlistService } from '../../../core/services/wishlist.service';

/**
 * Login page component.
 * Uses Angular Reactive Forms for validation — FormGroup holds the form state,
 * FormControl tracks individual field values and validation errors.
 */
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    RouterLink,
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private wishlistService = inject(WishlistService);
  private router = inject(Router);

  // FormGroup — a collection of FormControls that represent the whole form
  loginForm: FormGroup;

  // Tracks loading state to disable the button during the HTTP request
  isLoading = false;

  // Stores the error message returned by the backend e.g. "Bad credentials"
  errorMessage = '';

  // Controls password field visibility
  hidePassword = true;

  /** Inserted by Angular inject() migration for backwards compatibility */
  constructor(...args: unknown[]);

  constructor() {
    // Validators.required — field must not be empty
    // Validators.email    — field must match email format
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
    });
  }

  onSubmit() {
    // Mark all fields as touched to trigger validation messages on submit
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.authService.login(this.loginForm.value).subscribe({
      next: () => {
        // After login, load the wishlist and redirect to products
        this.wishlistService.loadWishlist().subscribe();
        this.router.navigate(['/products']);
      },
      error: (err) => {
        this.errorMessage =
          err.status === 401
            ? 'Invalid email or password.'
            : 'Something went wrong. Please try again.';
        this.isLoading = false;
      },
    });
  }
}
