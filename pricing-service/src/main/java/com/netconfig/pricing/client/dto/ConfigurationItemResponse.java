package com.netconfig.pricing.client.dto;

/**
 * DTO for configuration item data.
 */
public record ConfigurationItemResponse(
    String id,
    String productSku,
    String productName,
    int quantity
) {}

