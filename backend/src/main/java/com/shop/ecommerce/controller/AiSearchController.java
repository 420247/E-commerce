package com.shop.ecommerce.controller;

import com.shop.ecommerce.dto.AiSearchRequest;
import com.shop.ecommerce.dto.AiSearchResponse;
import com.shop.ecommerce.service.AiSearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for AI-powered product search.
 * Endpoint is public — no JWT required.
 * Base URL: /api/ai
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@CrossOrigin(origins = {
    "http://localhost",
    "http://localhost:4200",
    "https://e-commerce-dmxk.onrender.com"
})
public class AiSearchController {

    private final AiSearchService aiSearchService;

    /**
     * POST /api/ai/search
     * Accepts natural language query, returns matching products + explanation.
     *
     * Request body: { "query": "cheap phone under 500 euros" }
     * Response: { "products": [...], "explanation": "..." }
     */
    @PostMapping("/search")
    public ResponseEntity<AiSearchResponse> search(
            @Valid @RequestBody AiSearchRequest request
    ) throws Exception {
        return ResponseEntity.ok(aiSearchService.search(request.query()));
    }
}
