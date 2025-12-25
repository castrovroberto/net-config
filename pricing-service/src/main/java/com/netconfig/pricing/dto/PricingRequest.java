package com.netconfig.pricing.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * Request DTO for pricing calculation.
 */
public record PricingRequest(
    @NotBlank(message = "Configuration ID is required")
    String configurationId,
    
    String customerTier,  // STANDARD, PARTNER, ENTERPRISE
    
    Integer rackUnitsUsed,
    Integer rackCapacity,
    
    Map<String, Object> options  // include_support, support_tier, etc.
) {
    public PricingRequest {
        if (options == null) {
            options = Map.of();
        }
    }
}

