package com.netconfig.pricing.service;

import com.netconfig.pricing.domain.PricingResult;
import com.netconfig.pricing.dto.PricingRequest;
import com.netconfig.pricing.engine.PricingEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service layer for pricing operations.
 */
@Service
public class PricingService {

    private static final Logger log = LoggerFactory.getLogger(PricingService.class);

    private final PricingEngine pricingEngine;

    public PricingService(PricingEngine pricingEngine) {
        this.pricingEngine = pricingEngine;
    }

    /**
     * Calculate pricing for a configuration.
     */
    public PricingResult calculatePrice(PricingRequest request) {
        log.info("Processing pricing request for configuration: {}", request.configurationId());
        return pricingEngine.calculatePrice(request);
    }

    /**
     * Get list of active pricing strategies.
     */
    public List<String> getActiveStrategies() {
        return pricingEngine.getStrategyNames();
    }
}

