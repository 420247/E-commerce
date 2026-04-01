package com.shop.ecommerce.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Product entity mapped to the "products" table in the database. Represents a single item available
 * for purchase in the store.
 */
@Data // Lombok: generates getters, setters, toString, equals, hashCode
@Builder // Lombok: enables builder pattern e.g. Product.builder().name("...").build()
@NoArgsConstructor // Lombok: generates no-args constructor (required by JPA)
@AllArgsConstructor // Lombok: generates constructor with all fields
@Entity // JPA: marks this class as a database entity
@Table(name = "products") // JPA: maps to the "products" table
public class Product {

  @Id // Primary key
  @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment
  private Long id;

  @Column(nullable = false) // Product must always have a name
  private String name;

  @Column(columnDefinition = "TEXT") // TEXT allows unlimited length, unlike VARCHAR
  private String description;

  @Column(
      nullable = false,
      precision = 10,
      scale = 2) // precision=10 total digits, scale=2 decimal places e.g. 99999999.99
  private BigDecimal price; // BigDecimal is used for money — never use float/double for currency

  private String category; // e.g. "electronics", "clothing", "books"

  @Column(precision = 3, scale = 2) // e.g. 4.75 — max value is 9.99
  private BigDecimal rating; // Average customer rating

  @Column(name = "image_url") // Snake case in DB, camelCase in Java
  private String imageUrl; // URL to product image e.g. "https://cdn.example.com/img/product1.jpg"

  @Column(nullable = false)
  private Integer stock; // Number of items available in inventory

  @Column(name = "created_at")
  private LocalDateTime createdAt; // Timestamp when product was added to the store

  /**
   * Automatically sets createdAt before the entity is first saved to the database. @PrePersist runs
   * just before INSERT — no need to set it manually.
   */
  @PrePersist
  public void prePersist() {
    createdAt = LocalDateTime.now();
  }
}
