package com.netconfig.pricing.client.dto;

import java.math.BigDecimal;

/**
 * DTO for product data received from the Catalog Service.
 */
public record ProductResponse(
    String id,
    String sku,
    String name,
    String type,
    BigDecimal basePrice
) {}

