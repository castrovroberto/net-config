package com.netconfig.pricing.client;

import com.netconfig.pricing.client.dto.ProductResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
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

    private ProductResponse mapToProductResponse(Map<String, Object> data) {
        BigDecimal basePrice = BigDecimal.ZERO;
        Object priceObj = data.get("basePrice");
        if (priceObj instanceof Number) {
            basePrice = BigDecimal.valueOf(((Number) priceObj).doubleValue());
        }

        return new ProductResponse(
                (String) data.get("id"),
                (String) data.get("sku"),
                (String) data.get("name"),
                (String) data.get("type"),
                basePrice
        );
    }
}

