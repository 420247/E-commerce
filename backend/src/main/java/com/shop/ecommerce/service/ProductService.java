package com.shop.ecommerce.service;

import com.shop.ecommerce.model.Product;
import com.shop.ecommerce.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service for product business logic. Handles fetching and filtering products from the
 * database. @Service — marks class as a Spring service bean (singleton, managed by
 * Spring) @RequiredArgsConstructor — Lombok: injects dependencies via constructor
 */
@Service
@RequiredArgsConstructor
public class ProductService {

  private final ProductRepository productRepository; // JPA repository for DB operations

  /**
   * Returns all products, optionally filtered by category, price range and rating. All parameters
   * are optional — null means "no filter applied".
   *
   * <p>Example: getProducts("electronics", null, new BigDecimal("500"), new BigDecimal("4.0")) →
   * returns electronics under 500€ with rating >= 4.0
   *
   * @param category filter by category e.g. "electronics", null = all categories
   * @param minPrice minimum price inclusive, null = no lower bound
   * @param maxPrice maximum price inclusive, null = no upper bound
   * @param minRating minimum rating inclusive e.g. 4.0, null = any rating
   */
  public List<Product> getProducts(
      String category, BigDecimal minPrice, BigDecimal maxPrice, BigDecimal minRating) {
    return productRepository.findWithFilters(category, minPrice, maxPrice, minRating);
  }

  /**
   * Returns a single product by ID. Throws RuntimeException with 404-friendly message if product
   * not found.
   *
   * @param id product ID from the URL path e.g. /api/products/5
   */
  public Product getProductById(Long id) {
    return productRepository
        .findById(id)
        .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
  }
}
