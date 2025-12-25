package com.netconfig.quote.client;

import com.netconfig.quote.client.dto.PricingResponse;
import com.netconfig.quote.client.dto.PricingLineItemResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Client for communicating with the Pricing Service.
 */
@Component
public class PricingClient {

    private static final Logger log = LoggerFactory.getLogger(PricingClient.class);

    private final WebClient webClient;

    public PricingClient(@Value("${services.pricing.url}") String pricingServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(pricingServiceUrl)
                .build();
    }

    /**
     * Calculate pricing for a configuration.
     */
    public Optional<PricingResponse> calculatePrice(String configurationId, String customerTier,
                                                    boolean includeSupport, String supportTier) {
        try {
            Map<String, Object> request = new java.util.HashMap<>();
            request.put("configurationId", configurationId);
            if (customerTier != null) {
                request.put("customerTier", customerTier);
            }
            
            Map<String, Object> options = new java.util.HashMap<>();
            if (includeSupport) {
                options.put("include_support", true);
                options.put("support_tier", supportTier != null ? supportTier : "STANDARD");
            }
            request.put("options", options);

            var response = webClient.post()
                    .uri("/api/v1/pricing/calculate")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                return Optional.of(mapToPricingResponse(data));
            }
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to calculate pricing for configuration: {}", configurationId, e);
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    private PricingResponse mapToPricingResponse(Map<String, Object> data) {
        List<Map<String, Object>> lineItemsData = 
                (List<Map<String, Object>>) data.getOrDefault("lineItems", List.of());
        
        List<PricingLineItemResponse> lineItems = lineItemsData.stream()
                .map(item -> new PricingLineItemResponse(
                        (String) item.get("productSku"),
                        (String) item.get("productName"),
                        (String) item.get("productType"),
                        item.get("quantity") instanceof Number ? ((Number) item.get("quantity")).intValue() : 1,
                        toBigDecimal(item.get("unitPrice")),
                        toBigDecimal(item.get("lineTotal")),
                        toBigDecimal(item.get("discountAmount")),
                        (String) item.get("discountReason")
                ))
                .toList();

        List<String> discountDescriptions = 
                (List<String>) data.getOrDefault("discountDescriptions", List.of());

        return new PricingResponse(
                (String) data.get("configurationId"),
                lineItems,
                toBigDecimal(data.get("subtotal")),
                toBigDecimal(data.get("totalDiscount")),
                toBigDecimal(data.get("serviceAddOn")),
                toBigDecimal(data.get("grandTotal")),
                (String) data.getOrDefault("currency", "USD"),
                discountDescriptions
        );
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        return BigDecimal.ZERO;
    }
}

