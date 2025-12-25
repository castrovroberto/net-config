package com.netconfig.quote.service;

import com.netconfig.common.event.QuoteRequestedEvent;
import com.netconfig.common.exception.ResourceNotFoundException;
import com.netconfig.quote.client.ConfigurationClient;
import com.netconfig.quote.client.PricingClient;
import com.netconfig.quote.client.dto.ConfigurationResponse;
import com.netconfig.quote.client.dto.PricingLineItemResponse;
import com.netconfig.quote.client.dto.PricingResponse;
import com.netconfig.quote.domain.Quote;
import com.netconfig.quote.domain.QuoteLineItem;
import com.netconfig.quote.domain.QuoteStatus;
import com.netconfig.quote.dto.CreateQuoteRequest;
import com.netconfig.quote.messaging.QuoteEventPublisher;
import com.netconfig.quote.repository.QuoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Service for managing quotes.
 */
@Service
public class QuoteService {

    private static final Logger log = LoggerFactory.getLogger(QuoteService.class);

    private final QuoteRepository quoteRepository;
    private final QuoteNumberGenerator quoteNumberGenerator;
    private final ConfigurationClient configurationClient;
    private final PricingClient pricingClient;
    private final QuoteEventPublisher eventPublisher;

    public QuoteService(
            QuoteRepository quoteRepository,
            QuoteNumberGenerator quoteNumberGenerator,
            ConfigurationClient configurationClient,
            PricingClient pricingClient,
            QuoteEventPublisher eventPublisher) {
        this.quoteRepository = quoteRepository;
        this.quoteNumberGenerator = quoteNumberGenerator;
        this.configurationClient = configurationClient;
        this.pricingClient = pricingClient;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Create a new quote from a configuration.
     * This triggers async PDF generation.
     */
    @Transactional
    public Quote createQuote(CreateQuoteRequest request) {
        log.info("Creating quote for configuration: {}", request.configurationId());

        // Fetch configuration
        ConfigurationResponse configuration = configurationClient.getConfiguration(request.configurationId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Configuration not found: " + request.configurationId()));

        // Validate configuration
        if (!configuration.validated()) {
            throw new IllegalArgumentException(
                    "Configuration must be validated before creating a quote");
        }

        // Calculate pricing
        PricingResponse pricing = pricingClient.calculatePrice(
                request.configurationId(),
                request.customerTier(),
                request.includeSupport(),
                request.supportTier()
        ).orElseThrow(() -> new IllegalStateException(
                "Failed to calculate pricing for configuration: " + request.configurationId()));

        // Create quote with snapshot of current prices
        Quote quote = new Quote();
        quote.setQuoteNumber(quoteNumberGenerator.generate());
        quote.setConfigurationId(request.configurationId());
        quote.setCustomerId(request.customerId() != null ? request.customerId() : configuration.customerId());
        quote.setCustomerName(request.customerName());
        quote.setCustomerEmail(request.customerEmail());
        quote.setSubtotal(pricing.subtotal());
        quote.setTotalDiscount(pricing.totalDiscount());
        quote.setServiceAddOn(pricing.serviceAddOn());
        quote.setGrandTotal(pricing.grandTotal());
        quote.setCurrency(pricing.currency());
        quote.setStatus(QuoteStatus.PENDING);

        // Add line items as snapshot
        for (PricingLineItemResponse item : pricing.lineItems()) {
            QuoteLineItem lineItem = new QuoteLineItem(
                    item.productSku(),
                    item.productName(),
                    item.productType(),
                    item.quantity(),
                    item.unitPrice()
            );
            lineItem.setDiscountAmount(item.discountAmount());
            lineItem.setDiscountReason(item.discountReason());
            quote.addLineItem(lineItem);
        }

        // Save quote
        Quote savedQuote = quoteRepository.save(quote);
        log.info("Created quote: {} ({})", savedQuote.getQuoteNumber(), savedQuote.getId());

        // Publish event for async PDF generation
        eventPublisher.publishQuoteRequested(
                QuoteRequestedEvent.create(
                        savedQuote.getId(),
                        savedQuote.getConfigurationId(),
                        savedQuote.getCustomerEmail()
                )
        );

        return savedQuote;
    }

    /**
     * Get a quote by ID.
     */
    public Quote getQuote(String id) {
        return quoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quote", id));
    }

    /**
     * Get a quote by quote number.
     */
    public Quote getQuoteByNumber(String quoteNumber) {
        return quoteRepository.findByQuoteNumber(quoteNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Quote", quoteNumber));
    }

    /**
     * Get all quotes for a customer.
     */
    public List<Quote> getQuotesByCustomer(String customerId) {
        return quoteRepository.findByCustomerId(customerId);
    }

    /**
     * Get quotes by status.
     */
    public List<Quote> getQuotesByStatus(QuoteStatus status) {
        return quoteRepository.findByStatus(status);
    }

    /**
     * Get all quotes for a configuration.
     */
    public List<Quote> getQuotesByConfiguration(String configurationId) {
        return quoteRepository.findByConfigurationId(configurationId);
    }

    /**
     * Accept a quote.
     */
    @Transactional
    public Quote acceptQuote(String id) {
        Quote quote = getQuote(id);
        
        if (quote.isExpired()) {
            throw new IllegalStateException("Cannot accept expired quote: " + quote.getQuoteNumber());
        }
        
        if (quote.getStatus() != QuoteStatus.READY && quote.getStatus() != QuoteStatus.SENT) {
            throw new IllegalStateException(
                    "Quote must be in READY or SENT status to accept. Current: " + quote.getStatus());
        }
        
        quote.setStatus(QuoteStatus.ACCEPTED);
        Quote savedQuote = quoteRepository.save(quote);
        log.info("Quote accepted: {}", savedQuote.getQuoteNumber());
        
        return savedQuote;
    }

    /**
     * Reject a quote.
     */
    @Transactional
    public Quote rejectQuote(String id, String reason) {
        Quote quote = getQuote(id);
        
        if (quote.getStatus() == QuoteStatus.ACCEPTED) {
            throw new IllegalStateException("Cannot reject already accepted quote");
        }
        
        quote.setStatus(QuoteStatus.REJECTED);
        Quote savedQuote = quoteRepository.save(quote);
        log.info("Quote rejected: {} - {}", savedQuote.getQuoteNumber(), reason);
        
        return savedQuote;
    }

    /**
     * Mark quote as sent to customer.
     */
    @Transactional
    public Quote markAsSent(String id) {
        Quote quote = getQuote(id);
        
        if (quote.getStatus() != QuoteStatus.READY) {
            throw new IllegalStateException(
                    "Quote must be in READY status to send. Current: " + quote.getStatus());
        }
        
        quote.setStatus(QuoteStatus.SENT);
        Quote savedQuote = quoteRepository.save(quote);
        log.info("Quote marked as sent: {}", savedQuote.getQuoteNumber());
        
        return savedQuote;
    }

    /**
     * Regenerate PDF for a quote.
     */
    @Transactional
    public Quote regeneratePdf(String id) {
        Quote quote = getQuote(id);
        
        quote.setStatus(QuoteStatus.PENDING);
        quote.setPdfUrl(null);
        quote.setPdfGeneratedAt(null);
        Quote savedQuote = quoteRepository.save(quote);
        
        // Publish event for async PDF generation
        eventPublisher.publishQuoteRequested(
                QuoteRequestedEvent.create(
                        savedQuote.getId(),
                        savedQuote.getConfigurationId(),
                        savedQuote.getCustomerEmail()
                )
        );
        
        log.info("Triggered PDF regeneration for quote: {}", savedQuote.getQuoteNumber());
        return savedQuote;
    }

    /**
     * Expire old quotes.
     */
    @Transactional
    public int expireOldQuotes() {
        List<Quote> expiredQuotes = quoteRepository.findExpiredQuotes(Instant.now());
        
        for (Quote quote : expiredQuotes) {
            quote.setStatus(QuoteStatus.EXPIRED);
            quoteRepository.save(quote);
            log.info("Expired quote: {}", quote.getQuoteNumber());
        }
        
        return expiredQuotes.size();
    }

    /**
     * Get quote statistics.
     */
    public QuoteStats getStats() {
        return new QuoteStats(
                quoteRepository.count(),
                quoteRepository.countByStatus(QuoteStatus.PENDING),
                quoteRepository.countByStatus(QuoteStatus.READY),
                quoteRepository.countByStatus(QuoteStatus.ACCEPTED),
                quoteRepository.countByStatus(QuoteStatus.REJECTED),
                quoteRepository.countByStatus(QuoteStatus.EXPIRED)
        );
    }

    public record QuoteStats(
        long total,
        long pending,
        long ready,
        long accepted,
        long rejected,
        long expired
    ) {}
}

