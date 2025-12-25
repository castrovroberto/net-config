package com.netconfig.quote.controller;

import com.netconfig.common.dto.ApiResponse;
import com.netconfig.quote.domain.Quote;
import com.netconfig.quote.domain.QuoteStatus;
import com.netconfig.quote.dto.CreateQuoteRequest;
import com.netconfig.quote.dto.QuoteResponse;
import com.netconfig.quote.service.QuoteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for quote operations.
 */
@RestController
@RequestMapping("/api/v1/quotes")
public class QuoteController {

    private final QuoteService quoteService;

    public QuoteController(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    /**
     * Create a new quote.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<QuoteResponse>> createQuote(
            @Valid @RequestBody CreateQuoteRequest request) {
        Quote quote = quoteService.createQuote(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(QuoteResponse.from(quote), 
                        "Quote created successfully. PDF generation in progress."));
    }

    /**
     * Get a quote by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QuoteResponse>> getQuote(@PathVariable String id) {
        Quote quote = quoteService.getQuote(id);
        return ResponseEntity.ok(ApiResponse.success(QuoteResponse.from(quote)));
    }

    /**
     * Get a quote by quote number.
     */
    @GetMapping("/number/{quoteNumber}")
    public ResponseEntity<ApiResponse<QuoteResponse>> getQuoteByNumber(
            @PathVariable String quoteNumber) {
        Quote quote = quoteService.getQuoteByNumber(quoteNumber);
        return ResponseEntity.ok(ApiResponse.success(QuoteResponse.from(quote)));
    }

    /**
     * Get quotes by customer.
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<QuoteResponse>>> getQuotesByCustomer(
            @PathVariable String customerId) {
        List<Quote> quotes = quoteService.getQuotesByCustomer(customerId);
        List<QuoteResponse> responses = quotes.stream()
                .map(QuoteResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get quotes by status.
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<QuoteResponse>>> getQuotesByStatus(
            @PathVariable QuoteStatus status) {
        List<Quote> quotes = quoteService.getQuotesByStatus(status);
        List<QuoteResponse> responses = quotes.stream()
                .map(QuoteResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get quotes by configuration.
     */
    @GetMapping("/configuration/{configurationId}")
    public ResponseEntity<ApiResponse<List<QuoteResponse>>> getQuotesByConfiguration(
            @PathVariable String configurationId) {
        List<Quote> quotes = quoteService.getQuotesByConfiguration(configurationId);
        List<QuoteResponse> responses = quotes.stream()
                .map(QuoteResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Accept a quote.
     */
    @PostMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<QuoteResponse>> acceptQuote(@PathVariable String id) {
        Quote quote = quoteService.acceptQuote(id);
        return ResponseEntity.ok(ApiResponse.success(QuoteResponse.from(quote), 
                "Quote accepted successfully"));
    }

    /**
     * Reject a quote.
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<QuoteResponse>> rejectQuote(
            @PathVariable String id,
            @RequestParam(required = false, defaultValue = "Customer declined") String reason) {
        Quote quote = quoteService.rejectQuote(id, reason);
        return ResponseEntity.ok(ApiResponse.success(QuoteResponse.from(quote), 
                "Quote rejected"));
    }

    /**
     * Mark quote as sent.
     */
    @PostMapping("/{id}/send")
    public ResponseEntity<ApiResponse<QuoteResponse>> markAsSent(@PathVariable String id) {
        Quote quote = quoteService.markAsSent(id);
        return ResponseEntity.ok(ApiResponse.success(QuoteResponse.from(quote), 
                "Quote marked as sent"));
    }

    /**
     * Regenerate PDF for a quote.
     */
    @PostMapping("/{id}/regenerate-pdf")
    public ResponseEntity<ApiResponse<QuoteResponse>> regeneratePdf(@PathVariable String id) {
        Quote quote = quoteService.regeneratePdf(id);
        return ResponseEntity.ok(ApiResponse.success(QuoteResponse.from(quote), 
                "PDF regeneration triggered"));
    }

    /**
     * Get quote statistics.
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<QuoteService.QuoteStats>> getStats() {
        QuoteService.QuoteStats stats = quoteService.getStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * Get quote PDF (simulated).
     */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getQuotePdf(@PathVariable String id) {
        Quote quote = quoteService.getQuote(id);
        
        if (quote.getPdfUrl() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("PDF not yet generated"));
        }
        
        // In production, this would return the actual PDF or redirect to storage
        Map<String, Object> pdfInfo = Map.of(
                "quoteId", quote.getId(),
                "quoteNumber", quote.getQuoteNumber(),
                "pdfUrl", quote.getPdfUrl(),
                "generatedAt", quote.getPdfGeneratedAt(),
                "message", "In production, this would serve the actual PDF file"
        );
        
        return ResponseEntity.ok(ApiResponse.success(pdfInfo));
    }
}

