package com.shop.ecommerce.service;

import com.shop.ecommerce.model.Product;
import com.shop.ecommerce.model.User;
import com.shop.ecommerce.model.WishlistItem;
import com.shop.ecommerce.repository.ProductRepository;
import com.shop.ecommerce.repository.UserRepository;
import com.shop.ecommerce.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for wishlist business logic.
 * All operations require authenticated user — userId extracted from JWT in controller.
 *
 * @Transactional — ensures DB operations are atomic (all succeed or all roll back)
 */
@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    /**
     * Returns all wishlist items for a given user.
     * Called when user opens their wishlist page.
     *
     * @param userId extracted from JWT token in controller
     */
    public List<WishlistItem> getWishlist(Long userId) {
        return wishlistRepository.findByUserId(userId);
    }

    /**
     * Adds a product to user's wishlist.
     * Throws exception if product already in wishlist or product/user not found.
     *
     * @param userId    extracted from JWT token
     * @param productId from URL path e.g. /api/wishlist/5
     */
    @Transactional
    public WishlistItem addToWishlist(Long userId, Long productId) {

        // Check if already in wishlist — prevent duplicates
        if (wishlistRepository.findByUserIdAndProductId(userId, productId).isPresent()) {
            throw new IllegalArgumentException("Product already in wishlist");
        }

        // Load user and product — throw if not found
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Build and save the wishlist item
        WishlistItem item = WishlistItem.builder()
                .user(user)
                .product(product)
                .build();

        return wishlistRepository.save(item);
    }

    /**
     * Removes a product from user's wishlist.
     * Silent if product was not in wishlist.
     *
     * @param userId    extracted from JWT token
     * @param productId from URL path
     */
    @Transactional
    public void removeFromWishlist(Long userId, Long productId) {
        wishlistRepository.deleteByUserIdAndProductId(userId, productId);
    }
}
