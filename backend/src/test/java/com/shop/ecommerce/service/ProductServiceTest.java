package com.shop.ecommerce.service;

import com.shop.ecommerce.model.Product;
import com.shop.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product phone;
    private Product book;

    @BeforeEach
    void setUp() {
        phone = Product.builder()
                .id(1L)
                .name("iPhone 15 Pro")
                .category("electronics")
                .price(new BigDecimal("1199.99"))
                .rating(new BigDecimal("4.8"))
                .stock(50)
                .build();

        book = Product.builder()
                .id(2L)
                .name("Clean Code")
                .category("books")
                .price(new BigDecimal("39.99"))
                .rating(new BigDecimal("4.7"))
                .stock(25)
                .build();
    }

    // ── getProducts tests ───────────────────────────────────────────────────

    @Test
    @DisplayName("getProducts — no filters — returns all products")
    void getProducts_noFilters_returnsAll() {
        when(productRepository.findWithFilters(null, null, null, null))
                .thenReturn(List.of(phone, book));

        List<Product> result = productService.getProducts(null, null, null, null);

        assertThat(result).hasSize(2).containsExactly(phone, book);
    }

    @Test
    @DisplayName("getProducts — category filter — passes category to repository")
    void getProducts_categoryFilter_passesCorrectArgs() {
        when(productRepository.findWithFilters("electronics", null, null, null))
                .thenReturn(List.of(phone));

        List<Product> result = productService.getProducts("electronics", null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo("electronics");
        // Verify the repository was called with the exact category value
        verify(productRepository).findWithFilters("electronics", null, null, null);
    }

    @Test
    @DisplayName("getProducts — price range filter — passes correct bounds")
    void getProducts_priceRange_returnsProductsInRange() {
        BigDecimal min = new BigDecimal("10");
        BigDecimal max = new BigDecimal("100");

        when(productRepository.findWithFilters(null, min, max, null))
                .thenReturn(List.of(book));

        List<Product> result = productService.getProducts(null, min, max, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Clean Code");
    }

    @Test
    @DisplayName("getProducts — empty result — returns empty list, not null")
    void getProducts_noMatches_returnsEmptyList() {
        when(productRepository.findWithFilters(any(), any(), any(), any()))
                .thenReturn(List.of());

        List<Product> result = productService.getProducts("nonexistent", null, null, null);

        // Controller must never receive null — it would throw NPE when calling .size()
        assertThat(result).isNotNull().isEmpty();
    }

    // ── getProductById tests ────────────────────────────────────────────────

    @Test
    @DisplayName("getProductById — existing id — returns product")
    void getProductById_exists_returnsProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(phone));

        Product result = productService.getProductById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("iPhone 15 Pro");
    }

    @Test
    @DisplayName("getProductById — missing id — throws RuntimeException")
    void getProductById_notFound_throwsException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("99");
    }
}
