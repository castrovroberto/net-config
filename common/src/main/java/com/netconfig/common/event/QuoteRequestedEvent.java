package com.netconfig.common.event;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Event published when a quote is requested.
 * Consumed by async workers to generate PDF.
 */
public record QuoteRequestedEvent(
    String eventId,
    String quoteId,
    String configurationId,
    String customerEmail,
    Instant requestedAt
) implements Serializable {
    
    public static QuoteRequestedEvent create(String quoteId, String configurationId, String customerEmail) {
        return new QuoteRequestedEvent(
            UUID.randomUUID().toString(),
            quoteId,
            configurationId,
            customerEmail,
            Instant.now()
        );
    }
}

