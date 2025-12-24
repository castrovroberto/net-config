package com.netconfig.quote.messaging;

import com.netconfig.common.event.QuoteReadyEvent;
import com.netconfig.common.event.QuoteRequestedEvent;
import com.netconfig.quote.config.RabbitMQConfig;
import com.netconfig.quote.domain.Quote;
import com.netconfig.quote.domain.QuoteStatus;
import com.netconfig.quote.repository.QuoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Listens for quote events and processes them asynchronously.
 */
@Component
public class QuoteEventListener {

    private static final Logger log = LoggerFactory.getLogger(QuoteEventListener.class);

    private final QuoteRepository quoteRepository;
    private final QuoteEventPublisher eventPublisher;

    public QuoteEventListener(QuoteRepository quoteRepository, QuoteEventPublisher eventPublisher) {
        this.quoteRepository = quoteRepository;
        this.eventPublisher = eventPublisher;
    }

    @RabbitListener(queues = RabbitMQConfig.QUOTE_REQUESTED_QUEUE)
    public void handleQuoteRequested(QuoteRequestedEvent event) {
        log.info("Received QuoteRequestedEvent for quote: {}", event.quoteId());

        try {
            // Update status to GENERATING
            Quote quote = quoteRepository.findById(event.quoteId())
                    .orElseThrow(() -> new RuntimeException("Quote not found: " + event.quoteId()));

            quote.setStatus(QuoteStatus.GENERATING);
            quoteRepository.save(quote);

            // Simulate PDF generation (long-running process)
            log.info("Generating PDF for quote: {}...", event.quoteId());
            Thread.sleep(3000); // Simulate 3 second delay

            // Generate fake PDF URL
            String pdfUrl = String.format("/quotes/%s/pdf", event.quoteId());

            // Update quote with PDF info
            quote.setStatus(QuoteStatus.READY);
            quote.setPdfUrl(pdfUrl);
            quote.setPdfGeneratedAt(Instant.now());
            quoteRepository.save(quote);

            log.info("PDF generated for quote: {}", event.quoteId());

            // Publish QuoteReadyEvent
            eventPublisher.publishQuoteReady(QuoteReadyEvent.create(event.quoteId(), pdfUrl));

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("PDF generation interrupted for quote: {}", event.quoteId());
        } catch (Exception e) {
            log.error("Error processing quote: {}", event.quoteId(), e);
            // In production, you'd want to handle this properly (retry, DLQ, etc.)
        }
    }

    @RabbitListener(queues = RabbitMQConfig.QUOTE_READY_QUEUE)
    public void handleQuoteReady(QuoteReadyEvent event) {
        log.info("Quote {} is ready. PDF available at: {}", event.quoteId(), event.pdfUrl());
        // In production, you could send an email notification here
    }
}

