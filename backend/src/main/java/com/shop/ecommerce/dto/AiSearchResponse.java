package com.shop.ecommerce.dto;

import com.shop.ecommerce.model.Product;
import java.util.List;

/**
 * DTO returned after AI search.
 * Contains matched products and Claude's explanation.
 */
public record AiSearchResponse(
        List<Product> products,  // matching products from DB
        String explanation       // Claude's human-readable explanation of results
) {}
