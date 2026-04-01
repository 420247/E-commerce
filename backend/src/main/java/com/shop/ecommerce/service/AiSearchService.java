package com.shop.ecommerce.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.ecommerce.dto.AiSearchResponse;
import com.shop.ecommerce.model.Product;
import com.shop.ecommerce.repository.ProductRepository;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service for Claude AI-powered product search. Converts natural language query into product
 * filters using Anthropic API.
 *
 * <p>Flow: 1. User sends natural language query e.g. "cheap phone under 500 euros" 2. We send query
 * + available categories to Claude 3. Claude returns structured filters as JSON 4. We query DB with
 * those filters and return results @Slf4j — Lombok: generates a logger field, use log.info(),
 * log.error() etc.
 */
@Slf4j
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
   * Performs AI-powered product search. Sends user query to Claude, gets back filters, queries DB.
   *
   * @param query natural language search query from user
   */
  public AiSearchResponse search(String query) throws Exception {

    // Build prompt for Claude — instruct it to return only JSON
    String prompt =
        """
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
                """
            .formatted(query);

    // Build request body for Anthropic API
    String requestBody =
        objectMapper.writeValueAsString(
            new java.util.HashMap<>() {
              {
                put("model", model);
                put("max_tokens", 500);
                put(
                    "messages",
                    List.of(
                        new java.util.HashMap<>() {
                          {
                            put("role", "user");
                            put("content", prompt);
                          }
                        }));
              }
            });

    // Send HTTP request to Anthropic API using Java's built-in HttpClient
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create("https://api.anthropic.com/v1/messages"))
            .header("Content-Type", "application/json")
            .header("x-api-key", apiKey) // Anthropic API key
            .header("anthropic-version", "2023-06-01") // required API version header
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    // Log raw response for debugging
    log.info("Claude HTTP status: {}", response.statusCode());
    log.info("Claude raw response: {}", response.body());

    // Parse Claude's response
    JsonNode responseJson = objectMapper.readTree(response.body());

    // Check for API errors — Anthropic returns {"type": "error", "error": {...}} on failure
    if (responseJson.has("error")) {
      String errorMsg = responseJson.path("error").path("message").asText();
      log.error("Anthropic API error: {}", errorMsg);
      throw new RuntimeException("Claude API error: " + errorMsg);
    }

    // Validate response structure before accessing content
    if (!responseJson.has("content") || responseJson.path("content").size() == 0) {
      log.error("Unexpected Claude response structure: {}", response.body());
      throw new RuntimeException("Unexpected response from Claude API");
    }

    // Extract text from Claude's response content array
    String claudeText = responseJson.path("content").get(0).path("text").asText();

    log.info("Claude extracted text: {}", claudeText);

    // Parse the JSON filters Claude returned
    JsonNode filters = objectMapper.readTree(claudeText);

    // Extract filters — use null if not present or explicitly null
    String category =
        filters.path("category").isNull() || filters.path("category").isMissingNode()
            ? null
            : filters.path("category").asText();
    BigDecimal minPrice =
        filters.path("minPrice").isNull() || filters.path("minPrice").isMissingNode()
            ? null
            : new BigDecimal(filters.path("minPrice").asText());
    BigDecimal maxPrice =
        filters.path("maxPrice").isNull() || filters.path("maxPrice").isMissingNode()
            ? null
            : new BigDecimal(filters.path("maxPrice").asText());
    BigDecimal minRating =
        filters.path("minRating").isNull() || filters.path("minRating").isMissingNode()
            ? null
            : new BigDecimal(filters.path("minRating").asText());
    String explanation = filters.path("explanation").asText("No explanation provided");

    log.info(
        "Extracted filters — category: {}, minPrice: {}, maxPrice: {}, minRating: {}",
        category,
        minPrice,
        maxPrice,
        minRating);

    // Query DB with extracted filters
    List<Product> products =
        productRepository.findWithFilters(category, minPrice, maxPrice, minRating);

    log.info("Found {} products for query: {}", products.size(), query);

    return new AiSearchResponse(products, explanation);
  }
}
