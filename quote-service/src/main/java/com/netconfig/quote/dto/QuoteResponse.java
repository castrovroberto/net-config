package com.netconfig.quote.dto;

import com.netconfig.quote.domain.Quote;
import com.netconfig.quote.domain.QuoteLineItem;
import com.netconfig.quote.domain.QuoteStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Response DTO for quote data.
 */
public record QuoteResponse(
    String id,
    String quoteNumber,
    String configurationId,
    String customerId,
    String customerName,
    String customerEmail,
    List<LineItemResponse> lineItems,
    BigDecimal subtotal,
    BigDecimal totalDiscount,
    BigDecimal serviceAddOn,
    BigDecimal grandTotal,
    String currency,
    QuoteStatus status,
    String pdfUrl,
    Instant createdAt,
    Instant expiresAt,
    boolean expired
) {
    public static QuoteResponse from(Quote quote) {
        List<LineItemResponse> items = quote.getLineItems().stream()
                .map(LineItemResponse::from)
                .toList();
        
        return new QuoteResponse(
                quote.getId(),
                quote.getQuoteNumber(),
                quote.getConfigurationId(),
                quote.getCustomerId(),
                quote.getCustomerName(),
                quote.getCustomerEmail(),
                items,
                quote.getSubtotal(),
                quote.getTotalDiscount(),
                quote.getServiceAddOn(),
                quote.getGrandTotal(),
                quote.getCurrency(),
                quote.getStatus(),
                quote.getPdfUrl(),
                quote.getCreatedAt(),
                quote.getExpiresAt(),
                quote.isExpired()
        );
    }

    public record LineItemResponse(
        String productSku,
        String productName,
        String productType,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal,
        BigDecimal discountAmount,
        String discountReason
    ) {
        public static LineItemResponse from(QuoteLineItem item) {
            return new LineItemResponse(
                    item.getProductSku(),
                    item.getProductName(),
                    item.getProductType(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getLineTotal(),
                    item.getDiscountAmount(),
                    item.getDiscountReason()
            );
        }
    }
}

