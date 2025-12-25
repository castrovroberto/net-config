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
 * Support add-on strategy: Add 24/7 support cost as percentage of hardware.
 * Default: 20% of hardware cost for premium support.
 */
@Component
@Order(100) // Applied last, on final total
public class SupportAddOnStrategy implements PricingStrategy {

    public static final String SUPPORT_OPTION_KEY = "include_support";
    public static final String SUPPORT_TIER_KEY = "support_tier";
    
    private final BigDecimal standardSupportPercent;
    private final BigDecimal premiumSupportPercent;

    public SupportAddOnStrategy(
            @Value("${pricing.support-addon.standard-percent:15}") int standardPercent,
            @Value("${pricing.support-addon.premium-percent:20}") int premiumPercent) {
        this.standardSupportPercent = BigDecimal.valueOf(standardPercent).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        this.premiumSupportPercent = BigDecimal.valueOf(premiumPercent).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
    }

    @Override
    public PricingResult apply(PricingContext context, PricingResult currentResult) {
        if (!context.hasOption(SUPPORT_OPTION_KEY)) {
            return currentResult;
        }

        String supportTier = (String) context.getOptions().getOrDefault(SUPPORT_TIER_KEY, "STANDARD");
        BigDecimal supportPercent = "PREMIUM".equalsIgnoreCase(supportTier) 
                ? premiumSupportPercent 
                : standardSupportPercent;
        
        // Calculate support cost on hardware total (after discounts)
        BigDecimal hardwareTotal = currentResult.getSubtotal().subtract(currentResult.getTotalDiscount());
        BigDecimal supportCost = hardwareTotal
                .multiply(supportPercent)
                .setScale(2, RoundingMode.HALF_UP);
        
        currentResult.setServiceAddOn(supportCost);
        currentResult.addDiscountDescription(
                String.format("24/7 %s Support: %d%% of hardware ($%.2f) = $%.2f",
                        supportTier,
                        supportPercent.multiply(BigDecimal.valueOf(100)).intValue(),
                        hardwareTotal,
                        supportCost));
        currentResult.addAppliedStrategy(getName());
        currentResult.recalculateTotals();
        
        return currentResult;
    }

    @Override
    public String getName() {
        return "SupportAddOn";
    }

    @Override
    public int getOrder() {
        return 100;
    }
}

