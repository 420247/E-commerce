/**
 * Represents a single product available in the store.
 * Matches the Product entity returned by GET /api/products and GET /api/products/:id.
 */
export interface Product {
  id: number;
  name: string;
  description: string;
  /** Price in euros. Stored as DECIMAL(10,2) on the backend — no floating point errors. */
  price: number;
  /** One of: electronics, clothing, books, sports, home. */
  category: string;
  /** Average customer rating between 0.0 and 5.0. */
  rating: number;
  /** Full URL to the product image e.g. https://cdn.example.com/img/product1.jpg */
  imageUrl: string;
  /** Number of items currently in stock. */
  stock: number;
  createdAt: string;
}

/**
 * Query parameters for GET /api/products.
 * All fields are optional — omitting a field means no filter is applied for it.
 *
 * @example
 * // Electronics under 500€ with rating >= 4.0
 * { category: 'electronics', maxPrice: 500, minRating: 4.0 }
 */
export interface ProductFilter {
  category?: string;
  minPrice?: number;
  maxPrice?: number;
  minRating?: number;
}

/**
 * Response from POST /api/ai/search.
 * Contains the matched products and a human-readable explanation
 * of how Claude interpreted the search query.
 */
export interface AiSearchResponse {
  products: Product[];
  /** e.g. "Searched for electronics under 500€ with good ratings." */
  explanation: string;
}
