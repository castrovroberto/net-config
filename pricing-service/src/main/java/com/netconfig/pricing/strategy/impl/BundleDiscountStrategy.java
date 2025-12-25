package com.netconfig.pricing.strategy.impl;

import com.netconfig.pricing.domain.PricingContext;
import com.netconfig.pricing.domain.PricingResult;
import com.netconfig.pricing.strategy.PricingStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Bundle discount strategy: Apply discount when rack is well-utilized.
 * Default: 5% off total when rack capacity is >80% utilized.
 */
@Component
@Order(20)
public class BundleDiscountStrategy implements PricingStrategy {

    private final int capacityThreshold;
    private final BigDecimal discountPercent;

    public BundleDiscountStrategy(
            @Value("${pricing.bundle-discount.capacity-threshold:80}") int capacityThreshold,
            @Value("${pricing.bundle-discount.discount-percent:5}") int discountPercent) {
        this.capacityThreshold = capacityThreshold;
        this.discountPercent = BigDecimal.valueOf(discountPercent).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
    }

    @Override
    public PricingResult apply(PricingContext context, PricingResult currentResult) {
        Integer rackUtilization = context.getRackUtilizationPercent();
        
        if (rackUtilization != null && rackUtilization >= capacityThreshold) {
            BigDecimal currentTotal = currentResult.getSubtotal().subtract(currentResult.getTotalDiscount());
            BigDecimal bundleDiscount = currentTotal
                    .multiply(discountPercent)
                    .setScale(2, RoundingMode.HALF_UP);
            
            currentResult.addOrderDiscount(bundleDiscount);
            currentResult.addDiscountDescription(
                    String.format("Bundle discount: %d%% off (rack %d%% utilized, threshold %d%%) - saved $%.2f",
                            discountPercent.multiply(BigDecimal.valueOf(100)).intValue(),
                            rackUtilization,
                            capacityThreshold,
                            bundleDiscount));
            currentResult.addAppliedStrategy(getName());
        }
        
        return currentResult;
    }

    @Override
    public String getName() {
        return "BundleDiscount";
    }

    @Override
    public int getOrder() {
        return 20;
    }
}

