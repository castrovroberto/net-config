package com.netconfig.common.event;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Event published when a quote PDF is ready.
 * Can be used to notify the user or trigger downstream processes.
 */
public record QuoteReadyEvent(
    String eventId,
    String quoteId,
    String pdfUrl,
    Instant generatedAt
) implements Serializable {
    
    public static QuoteReadyEvent create(String quoteId, String pdfUrl) {
        return new QuoteReadyEvent(
            UUID.randomUUID().toString(),
            quoteId,
            pdfUrl,
            Instant.now()
        );
    }
}

