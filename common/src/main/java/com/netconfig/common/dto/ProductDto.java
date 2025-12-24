package com.netconfig.common.dto;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Data Transfer Object for Product information.
 * Used across services to share product data.
 */
public record ProductDto(
    String id,
    String sku,
    String name,
    String description,
    ProductType type,
    BigDecimal basePrice,
    Map<String, Object> attributes,
    Map<String, Object> compatibilityRules
) {
    public enum ProductType {
        RACK,
        SWITCH,
        PSU,
        CABLE,
        SFP_MODULE,
        ACCESSORY
    }
}

