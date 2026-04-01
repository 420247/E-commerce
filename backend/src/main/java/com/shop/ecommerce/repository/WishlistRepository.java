package com.shop.ecommerce.repository;

import com.shop.ecommerce.model.WishlistItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository for WishlistItem entity. */
@Repository
public interface WishlistRepository extends JpaRepository<WishlistItem, Long> {

  /** Get all wishlist items for a specific user. Called when user opens their wishlist page. */
  List<WishlistItem> findByUserId(Long userId);

  /**
   * Find a specific wishlist item by user and product. Used to check if product is already in
   * wishlist.
   */
  Optional<WishlistItem> findByUserIdAndProductId(Long userId, Long productId);

  /** Delete a specific wishlist item by user and product. */
  void deleteByUserIdAndProductId(Long userId, Long productId);
}
