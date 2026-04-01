package com.shop.ecommerce.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.shop.ecommerce.model.Product;
import com.shop.ecommerce.model.Role;
import com.shop.ecommerce.model.User;
import com.shop.ecommerce.model.WishlistItem;
import com.shop.ecommerce.repository.ProductRepository;
import com.shop.ecommerce.repository.UserRepository;
import com.shop.ecommerce.repository.WishlistRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

  @Mock private WishlistRepository wishlistRepository;
  @Mock private ProductRepository productRepository;
  @Mock private UserRepository userRepository;

  @InjectMocks private WishlistService wishlistService;

  private User user;
  private Product product;

  @BeforeEach
  void setUp() {
    user =
        User.builder()
            .id(1L)
            .name("Alice")
            .email("alice@example.com")
            .password("hashed")
            .role(Role.USER)
            .build();

    product =
        Product.builder()
            .id(10L)
            .name("Sony WH-1000XM5")
            .category("electronics")
            .price(new BigDecimal("349.99"))
            .rating(new BigDecimal("4.8"))
            .stock(40)
            .build();
  }

  // ── getWishlist tests ───────────────────────────────────────────────────

  @Test
  @DisplayName("getWishlist — returns all items for the user")
  void getWishlist_returnsUserItems() {
    WishlistItem item = WishlistItem.builder().id(1L).user(user).product(product).build();
    when(wishlistRepository.findByUserId(1L)).thenReturn(List.of(item));

    List<WishlistItem> result = wishlistService.getWishlist(1L);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getProduct().getName()).isEqualTo("Sony WH-1000XM5");
  }

  @Test
  @DisplayName("getWishlist — empty wishlist — returns empty list")
  void getWishlist_empty_returnsEmptyList() {
    when(wishlistRepository.findByUserId(1L)).thenReturn(List.of());

    List<WishlistItem> result = wishlistService.getWishlist(1L);

    assertThat(result).isNotNull().isEmpty();
  }

  // ── addToWishlist tests ─────────────────────────────────────────────────

  @Test
  @DisplayName("addToWishlist — new item — saves and returns WishlistItem")
  void addToWishlist_newItem_savedSuccessfully() {
    // Product not yet in wishlist
    when(wishlistRepository.findByUserIdAndProductId(1L, 10L)).thenReturn(Optional.empty());
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(productRepository.findById(10L)).thenReturn(Optional.of(product));

    WishlistItem saved = WishlistItem.builder().id(1L).user(user).product(product).build();
    when(wishlistRepository.save(any(WishlistItem.class))).thenReturn(saved);

    WishlistItem result = wishlistService.addToWishlist(1L, 10L);

    assertThat(result.getProduct().getId()).isEqualTo(10L);
    verify(wishlistRepository, times(1)).save(any(WishlistItem.class));
  }

  @Test
  @DisplayName("addToWishlist — duplicate — throws IllegalArgumentException")
  void addToWishlist_alreadyInWishlist_throwsException() {
    // Product is already in the wishlist
    WishlistItem existing = WishlistItem.builder().id(1L).user(user).product(product).build();
    when(wishlistRepository.findByUserIdAndProductId(1L, 10L)).thenReturn(Optional.of(existing));

    assertThatThrownBy(() -> wishlistService.addToWishlist(1L, 10L))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("already in wishlist");

    // Must not save a duplicate
    verify(wishlistRepository, never()).save(any());
  }

  @Test
  @DisplayName("addToWishlist — product not found — throws RuntimeException")
  void addToWishlist_productNotFound_throwsException() {
    when(wishlistRepository.findByUserIdAndProductId(anyLong(), anyLong()))
        .thenReturn(Optional.empty());
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(productRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> wishlistService.addToWishlist(1L, 99L))
        .isInstanceOf(RuntimeException.class);
  }

  // ── removeFromWishlist tests ────────────────────────────────────────────

  @Test
  @DisplayName("removeFromWishlist — delegates to repository delete method")
  void removeFromWishlist_callsDeleteOnRepository() {
    doNothing().when(wishlistRepository).deleteByUserIdAndProductId(1L, 10L);

    wishlistService.removeFromWishlist(1L, 10L);

    verify(wishlistRepository, times(1)).deleteByUserIdAndProductId(1L, 10L);
  }
}
