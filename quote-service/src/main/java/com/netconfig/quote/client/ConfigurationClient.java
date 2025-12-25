package com.netconfig.quote.client;

import com.netconfig.quote.client.dto.ConfigurationResponse;
import com.netconfig.quote.client.dto.ConfigurationItemResponse;
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
 * Client for communicating with the Configuration Service.
 */
@Component
public class ConfigurationClient {

    private static final Logger log = LoggerFactory.getLogger(ConfigurationClient.class);

    private final WebClient webClient;

    public ConfigurationClient(@Value("${services.configuration.url}") String configurationServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(configurationServiceUrl)
                .build();
    }

    /**
     * Fetch a configuration by its ID.
     */
    public Optional<ConfigurationResponse> getConfiguration(String configurationId) {
        try {
            var response = webClient.get()
                    .uri("/api/v1/configurations/{id}", configurationId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                return Optional.of(mapToConfigurationResponse(data));
            }
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to fetch configuration: {}", configurationId, e);
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    private ConfigurationResponse mapToConfigurationResponse(Map<String, Object> data) {
        List<Map<String, Object>> itemsData = (List<Map<String, Object>>) data.getOrDefault("items", List.of());
        
        List<ConfigurationItemResponse> items = itemsData.stream()
                .map(item -> new ConfigurationItemResponse(
                        (String) item.get("id"),
                        (String) item.get("productSku"),
                        (String) item.get("productName"),
                        item.get("quantity") instanceof Number ? ((Number) item.get("quantity")).intValue() : 1
                ))
                .toList();

        return new ConfigurationResponse(
                (String) data.get("id"),
                (String) data.get("name"),
                (String) data.get("customerId"),
                (String) data.get("rackSku"),
                Boolean.TRUE.equals(data.get("validated")),
                items
        );
    }
}

