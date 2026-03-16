package com.shop.ecommerce.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.ecommerce.dto.AiSearchResponse;
import com.shop.ecommerce.model.Product;
import com.shop.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * Service for Claude AI-powered product search.
 * Converts natural language query into product filters using Anthropic API.
 *
 * Flow:
 * 1. User sends natural language query e.g. "cheap phone under 500 euros"
 * 2. We send query + available categories to Claude
 * 3. Claude returns structured filters as JSON
 * 4. We query DB with those filters and return results
 */
@Service
@RequiredArgsConstructor
public class AiSearchService {

    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper; // Jackson JSON parser — provided by Spring Boot

    @Value("${application.anthropic.api-key}")
    private String apiKey;

    @Value("${application.anthropic.model}")
    private String model;

    /**
     * Performs AI-powered product search.
     * Sends user query to Claude, gets back filters, queries DB.
     *
     * @param query natural language search query from user
     */
    public AiSearchResponse search(String query) throws Exception {

        // Build prompt for Claude — instruct it to return only JSON
        String prompt = """
                You are a product search assistant for an e-commerce store.
                Available categories: electronics, clothing, books, sports, home.
                
                User query: "%s"
                
                Extract search filters from the query and respond ONLY with a JSON object like this:
                {
                  "category": "electronics or null",
                  "minPrice": null,
                  "maxPrice": null,
                  "minRating": null,
                  "explanation": "short explanation of what you searched for"
                }
                
                Rules:
                - Use null for filters that are not mentioned
                - category must be one of: electronics, clothing, books, sports, home, or null
                - prices are in euros
                - minRating is between 0.0 and 5.0
                - Return ONLY the JSON, no other text
                """.formatted(query);

        // Build request body for Anthropic API
        String requestBody = objectMapper.writeValueAsString(new java.util.HashMap<>() {{
            put("model", model);
            put("max_tokens", 500);
            put("messages", List.of(new java.util.HashMap<>() {{
                put("role", "user");
                put("content", prompt);
            }}));
        }});

        // Send HTTP request to Anthropic API using Java's built-in HttpClient
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.anthropic.com/v1/messages"))
                .header("Content-Type", "application/json")
                .header("x-api-key", apiKey)                    // Anthropic API key
                .header("anthropic-version", "2023-06-01")      // required API version header
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Parse Claude's response
        JsonNode responseJson = objectMapper.readTree(response.body());
        String claudeText = responseJson
                .path("content").get(0)
                .path("text").asText();

        // Parse the JSON filters Claude returned
        JsonNode filters = objectMapper.readTree(claudeText);

        String category = filters.path("category").isNull() ? null : filters.path("category").asText();
        BigDecimal minPrice = filters.path("minPrice").isNull() ? null : new BigDecimal(filters.path("minPrice").asText());
        BigDecimal maxPrice = filters.path("maxPrice").isNull() ? null : new BigDecimal(filters.path("maxPrice").asText());
        BigDecimal minRating = filters.path("minRating").isNull() ? null : new BigDecimal(filters.path("minRating").asText());
        String explanation = filters.path("explanation").asText();

        // Query DB with extracted filters
        List<Product> products = productRepository.findWithFilters(category, minPrice, maxPrice, minRating);

        return new AiSearchResponse(products, explanation);
    }
}
