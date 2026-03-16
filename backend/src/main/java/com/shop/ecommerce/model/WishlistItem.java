package com.shop.ecommerce.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * WishlistItem entity — links a user to a saved product.
 * Mapped to the "wishlist_items" table.
 * One row = one product saved by one user.
 */
@Data               // Lombok: generates getters, setters, toString, equals, hashCode
@Builder            // Lombok: enables builder pattern
@NoArgsConstructor  // Lombok: required by JPA
@AllArgsConstructor // Lombok: constructor with all fields
@Entity
@Table(name = "wishlist_items")
public class WishlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment
    private Long id;

    /**
     * Many wishlist items can belong to one user.
     * @ManyToOne — many items → one user (foreign key: user_id)
     * FetchType.LAZY — user data is NOT loaded automatically, only when accessed.
     *                  Prevents unnecessary DB queries.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // FK column in wishlist_items table
    private User user;

    /**
     * Many wishlist items can reference the same product.
     * @ManyToOne — many items → one product (foreign key: product_id)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false) // FK column in wishlist_items table
    private Product product;

    @Column(name = "added_at")
    private LocalDateTime addedAt; // Timestamp when user added this product to wishlist

    /**
     * Automatically sets addedAt before first save.
     * @PrePersist runs just before INSERT.
     */
    @PrePersist
    public void prePersist() {
        addedAt = LocalDateTime.now();
    }
}
