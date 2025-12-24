package com.netconfig.quote.repository;

import com.netconfig.quote.domain.Quote;
import com.netconfig.quote.domain.QuoteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * JPA repository for Quote entities.
 */
@Repository
public interface QuoteRepository extends JpaRepository<Quote, String> {

    Optional<Quote> findByQuoteNumber(String quoteNumber);

    List<Quote> findByCustomerId(String customerId);

    List<Quote> findByStatus(QuoteStatus status);

    List<Quote> findByConfigurationId(String configurationId);

    @Query("SELECT q FROM Quote q WHERE q.expiresAt < :now AND q.status NOT IN ('EXPIRED', 'ACCEPTED', 'REJECTED')")
    List<Quote> findExpiredQuotes(Instant now);

    @Query("SELECT q FROM Quote q WHERE q.customerId = :customerId ORDER BY q.createdAt DESC")
    List<Quote> findRecentByCustomerId(String customerId);

    @Query("SELECT COUNT(q) FROM Quote q WHERE q.status = :status")
    long countByStatus(QuoteStatus status);
}

