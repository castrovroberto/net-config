package com.netconfig.quote.client.dto;

import java.math.BigDecimal;

/**
 * DTO for pricing line item data.
 */
public record PricingLineItemResponse(
    String productSku,
    String productName,
    String productType,
    int quantity,
    BigDecimal unitPrice,
    BigDecimal lineTotal,
    BigDecimal discountAmount,
    String discountReason
) {}

