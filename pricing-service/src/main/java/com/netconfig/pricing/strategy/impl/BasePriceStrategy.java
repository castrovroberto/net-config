package com.netconfig.pricing.strategy.impl;

import com.netconfig.pricing.domain.PricingContext;
import com.netconfig.pricing.domain.PricingLineItem;
import com.netconfig.pricing.domain.PricingResult;
import com.netconfig.pricing.strategy.PricingStrategy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Base pricing strategy that calculates the raw price from catalog prices.
 * This is always the first strategy to run.
 */
@Component
@Order(1)
public class BasePriceStrategy implements PricingStrategy {

    @Override
    public PricingResult apply(PricingContext context, PricingResult currentResult) {
        // Initialize line items from context
        currentResult.setLineItems(new ArrayList<>(context.getLineItems()));
        
        // Calculate subtotal from line items
        BigDecimal subtotal = context.getLineItems().stream()
                .map(PricingLineItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        currentResult.setSubtotal(subtotal);
        currentResult.setGrandTotal(subtotal);
        currentResult.addAppliedStrategy(getName());
        
        return currentResult;
    }

    @Override
    public String getName() {
        return "BasePrice";
    }

    @Override
    public int getOrder() {
        return 1;
    }
}

