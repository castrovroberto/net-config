package com.netconfig.pricing.strategy;

import com.netconfig.pricing.domain.PricingContext;
import com.netconfig.pricing.domain.PricingResult;

/**
 * Strategy interface for pricing calculations.
 * Implementations can calculate base prices, discounts, or add-ons.
 */
public interface PricingStrategy {

    /**
     * Calculate or adjust pricing based on the context.
     *
     * @param context The pricing context with configuration and customer info
     * @param currentResult The current pricing result (from previous strategies)
     * @return Updated pricing result
     */
    PricingResult apply(PricingContext context, PricingResult currentResult);

    /**
     * Get the name of this pricing strategy for logging/debugging.
     */
    String getName();

    /**
     * Order in which this strategy should be applied (lower = earlier).
     */
    default int getOrder() {
        return 100;
    }
}

