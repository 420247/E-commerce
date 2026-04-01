import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Product, ProductFilter, AiSearchResponse } from '../../shared/models/product.model';
import { environment } from '../../../environments/environment';

/**
 * Handles all product-related HTTP requests to the backend.
 * Provides methods for fetching products with filters and performing AI-powered search.
 */
@Injectable({ providedIn: 'root' })
export class ProductService {
  private http = inject(HttpClient);

  private apiUrl = `${environment.apiUrl}/products`;
  private aiUrl = `${environment.apiUrl}/ai`;

  /** Inserted by Angular inject() migration for backwards compatibility */
  constructor(...args: unknown[]);

  constructor() {}

  /**
   * Fetches products from GET /api/products with optional filters.
   * Only defined filter fields are appended as query parameters.
   *
   * @example
   * // GET /api/products?category=electronics&maxPrice=500
   * this.productService.getProducts({ category: 'electronics', maxPrice: 500 });
   */
  getProducts(filter?: ProductFilter): Observable<Product[]> {
    let params = new HttpParams();
    if (filter?.category) params = params.set('category', filter.category);
    if (filter?.minPrice) params = params.set('minPrice', filter.minPrice);
    if (filter?.maxPrice) params = params.set('maxPrice', filter.maxPrice);
    if (filter?.minRating) params = params.set('minRating', filter.minRating);
    return this.http.get<Product[]>(this.apiUrl, { params });
  }

  /**
   * Fetches a single product by ID from GET /api/products/:id.
   * Used on the product detail page.
   */
  getProductById(id: number): Observable<Product> {
    return this.http.get<Product>(`${this.apiUrl}/${id}`);
  }

  /**
   * Sends a natural language query to POST /api/ai/search.
   * The backend forwards the query to Claude, which extracts filters
   * and returns matching products along with an explanation.
   *
   * @example
   * this.productService.aiSearch('cheap phone with good camera under 500 euros');
   */
  aiSearch(query: string): Observable<AiSearchResponse> {
    return this.http.post<AiSearchResponse>(`${this.aiUrl}/search`, { query });
  }
}
