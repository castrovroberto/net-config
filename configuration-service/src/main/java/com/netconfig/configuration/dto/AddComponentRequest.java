package com.netconfig.configuration.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for adding a component to a configuration.
 */
public record AddComponentRequest(
    @NotBlank(message = "Product SKU is required")
    String productSku,

    @Min(value = 1, message = "Quantity must be at least 1")
    int quantity,

    Integer rackPosition
) {
    public AddComponentRequest {
        if (quantity <= 0) {
            quantity = 1;
        }
    }
}

