package com.netconfig.quote.scheduler;

import com.netconfig.quote.service.QuoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled task to expire old quotes.
 */
@Component
@EnableScheduling
public class QuoteExpirationScheduler {

    private static final Logger log = LoggerFactory.getLogger(QuoteExpirationScheduler.class);

    private final QuoteService quoteService;

    public QuoteExpirationScheduler(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    /**
     * Run daily at midnight to expire old quotes.
     */
    @Scheduled(cron = "${quote.expiration.cron:0 0 0 * * *}")
    public void expireQuotes() {
        log.info("Running quote expiration job...");
        int count = quoteService.expireOldQuotes();
        log.info("Expired {} quotes", count);
    }
}

