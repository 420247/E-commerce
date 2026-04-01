package com.shop.ecommerce.controller;

import com.shop.ecommerce.model.User;
import com.shop.ecommerce.model.WishlistItem;
import com.shop.ecommerce.service.WishlistService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for wishlist endpoints. All endpoints require JWT token — configured in
 * SecurityConfig (.anyRequest().authenticated()) Base URL: /api/wishlist @AuthenticationPrincipal —
 * Spring Security injects the currently authenticated User object extracted from the JWT token
 * automatically by JwtAuthenticationFilter
 */
@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@CrossOrigin(
    origins = {"http://localhost", "http://localhost:4200", "https://e-commerce-dmxk.onrender.com"})
public class WishlistController {

  private final WishlistService wishlistService;

  /**
   * GET /api/wishlist Returns all wishlist items for the authenticated user. Called on page load —
   * Angular fetches wishlist after login.
   */
  @GetMapping
  public ResponseEntity<List<WishlistItem>> getWishlist(
      @AuthenticationPrincipal User currentUser // injected from JWT, no DB call needed
      ) {
    return ResponseEntity.ok(wishlistService.getWishlist(currentUser.getId()));
  }

  /**
   * POST /api/wishlist/{productId} Adds a product to the authenticated user's wishlist. Returns the
   * created WishlistItem with 200 OK. @PathVariable — extracts productId from URL e.g.
   * /api/wishlist/3
   */
  @PostMapping("/{productId}")
  public ResponseEntity<WishlistItem> addToWishlist(
      @AuthenticationPrincipal User currentUser, @PathVariable Long productId) {
    return ResponseEntity.ok(wishlistService.addToWishlist(currentUser.getId(), productId));
  }

  /**
   * DELETE /api/wishlist/{productId} Removes a product from the authenticated user's wishlist.
   * Returns 204 No Content — successful deletion with no body.
   */
  @DeleteMapping("/{productId}")
  public ResponseEntity<Void> removeFromWishlist(
      @AuthenticationPrincipal User currentUser, @PathVariable Long productId) {
    wishlistService.removeFromWishlist(currentUser.getId(), productId);
    return ResponseEntity.noContent().build(); // 204 No Content
  }
}
