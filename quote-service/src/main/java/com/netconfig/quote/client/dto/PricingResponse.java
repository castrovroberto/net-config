package com.netconfig.quote.client.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for pricing data received from the Pricing Service.
 */
public record PricingResponse(
    String configurationId,
    List<PricingLineItemResponse> lineItems,
    BigDecimal subtotal,
    BigDecimal totalDiscount,
    BigDecimal serviceAddOn,
    BigDecimal grandTotal,
    String currency,
    List<String> discountDescriptions
) {}

