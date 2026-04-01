package com.shop.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;

/** DTO for AI search request. Contains the natural language query from the user. */
public record AiSearchRequest(
    @NotBlank(message = "Query is required")
        String query // e.g. "cheap phone with good camera under 500 euros"
    ) {}
