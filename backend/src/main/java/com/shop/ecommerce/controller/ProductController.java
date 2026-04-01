package com.shop.ecommerce.controller;

import com.shop.ecommerce.model.Product;
import com.shop.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST controller for product endpoints.
 * All endpoints are public — no JWT required (configured in SecurityConfig).
 * Base URL: /api/products
 *
 * @RestController — marks class as REST controller, methods return JSON
 * @RequestMapping — sets base URL prefix for all endpoints
 * @CrossOrigin    — allows Angular dev server (port 4200) to call this API
 * @RequiredArgsConstructor — Lombok: constructor injection
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = {
    "http://localhost",
    "http://localhost:4200",
    "https://e-commerce-dmxk.onrender.com"
})
public class ProductController {

    private final ProductService productService;

    /**
     * GET /api/products
     * Returns all products, optionally filtered via query parameters.
     *
     * Example URLs:
     * /api/products                              → all products
     * /api/products?category=electronics        → only electronics
     * /api/products?minPrice=10&maxPrice=100    → price between 10-100
     * /api/products?minRating=4.0               → rating >= 4.0
     * /api/products?category=books&maxPrice=50  → books under 50€
     *
     * @RequestParam(required = false) — query param is optional, defaults to null
     */
    @GetMapping
    public ResponseEntity<List<Product>> getProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) BigDecimal minRating
    ) {
        List<Product> products = productService.getProducts(category, minPrice, maxPrice, minRating);
        return ResponseEntity.ok(products);
    }

    /**
     * GET /api/products/{id}
     * Returns a single product by ID.
     * Returns 200 OK with product, or 500 if not found (we'll improve this later).
     *
     * @PathVariable — extracts {id} from the URL path e.g. /api/products/5 → id=5
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }
}
