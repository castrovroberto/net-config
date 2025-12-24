package com.netconfig.quote.domain;

/**
 * Status of a quote in its lifecycle.
 */
public enum QuoteStatus {
    PENDING,      // Quote created, PDF generation pending
    GENERATING,   // PDF is being generated
    READY,        // Quote and PDF are ready
    SENT,         // Quote has been sent to customer
    ACCEPTED,     // Customer accepted the quote
    REJECTED,     // Customer rejected the quote
    EXPIRED       // Quote validity period has passed
}

