package com.netconfig.configuration.client;

import com.netconfig.configuration.client.dto.ProductResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Client for communicating with the Catalog Service.
 */
@Component
public class CatalogClient {

    private static final Logger log = LoggerFactory.getLogger(CatalogClient.class);

    private final WebClient webClient;

    public CatalogClient(@Value("${services.catalog.url}") String catalogServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(catalogServiceUrl)
                .build();
    }

    /**
     * Fetch a product by its SKU.
     */
    public Optional<ProductResponse> getProductBySku(String sku) {
        try {
            var response = webClient.get()
                    .uri("/api/v1/products/sku/{sku}", sku)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                return Optional.of(mapToProductResponse(data));
            }
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to fetch product with SKU: {}", sku, e);
            return Optional.empty();
        }
    }

    /**
     * Fetch all products of a specific type.
     */
    public List<ProductResponse> getProductsByType(String type) {
        try {
            var response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/products")
                            .queryParam("type", type)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> dataList = (List<Map<String, Object>>) response.get("data");
                return dataList.stream()
                        .map(this::mapToProductResponse)
                        .toList();
            }
            return List.of();
        } catch (Exception e) {
            log.error("Failed to fetch products of type: {}", type, e);
            return List.of();
        }
    }

    private ProductResponse mapToProductResponse(Map<String, Object> data) {
        @SuppressWarnings("unchecked")
        Map<String, Object> attributes = (Map<String, Object>) data.getOrDefault("attributes", Map.of());
        @SuppressWarnings("unchecked")
        Map<String, Object> compatibilityRules = (Map<String, Object>) data.getOrDefault("compatibilityRules", Map.of());

        return new ProductResponse(
                (String) data.get("id"),
                (String) data.get("sku"),
                (String) data.get("name"),
                (String) data.get("type"),
                data.get("basePrice") != null ? ((Number) data.get("basePrice")).doubleValue() : 0.0,
                attributes,
                compatibilityRules
        );
    }
}

