package com.netconfig.quote.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for creating a new quote.
 */
public record CreateQuoteRequest(
    @NotBlank(message = "Configuration ID is required")
    String configurationId,
    
    String customerId,
    
    String customerName,
    
    @Email(message = "Valid email is required")
    String customerEmail,
    
    String customerTier,  // STANDARD, PARTNER, ENTERPRISE
    
    boolean includeSupport,
    
    String supportTier  // STANDARD, PREMIUM
) {}

