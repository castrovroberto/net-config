package com.netconfig.quote.service;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates unique quote numbers.
 * Format: QT-YYYYMMDD-XXXXX (e.g., QT-20251225-00001)
 */
@Component
public class QuoteNumberGenerator {

    private static final String PREFIX = "QT";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    private final AtomicLong sequence = new AtomicLong(0);
    private String currentDate = "";

    /**
     * Generate a new unique quote number.
     */
    public synchronized String generate() {
        String today = LocalDate.now().format(DATE_FORMAT);
        
        // Reset sequence if date changed
        if (!today.equals(currentDate)) {
            currentDate = today;
            sequence.set(0);
        }
        
        long seq = sequence.incrementAndGet();
        return String.format("%s-%s-%05d", PREFIX, today, seq);
    }
}

