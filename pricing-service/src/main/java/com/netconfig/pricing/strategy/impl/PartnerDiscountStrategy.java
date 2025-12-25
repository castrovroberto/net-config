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
 * Partner discount strategy: Apply discount for partner-tier customers.
 * Default: 15% off for PARTNER tier customers.
 */
@Component
@Order(30)
public class PartnerDiscountStrategy implements PricingStrategy {

    private static final String PARTNER_TIER = "PARTNER";
    private static final String ENTERPRISE_TIER = "ENTERPRISE";
    
    private final BigDecimal partnerDiscountPercent;
    private final BigDecimal enterpriseDiscountPercent;

    public PartnerDiscountStrategy(
            @Value("${pricing.partner-discount.percent:15}") int partnerPercent,
            @Value("${pricing.enterprise-discount.percent:20}") int enterprisePercent) {
        this.partnerDiscountPercent = BigDecimal.valueOf(partnerPercent).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        this.enterpriseDiscountPercent = BigDecimal.valueOf(enterprisePercent).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
    }

    @Override
    public PricingResult apply(PricingContext context, PricingResult currentResult) {
        String customerTier = context.getCustomerTier();
        
        if (customerTier == null) {
            return currentResult;
        }

        BigDecimal discountPercent = null;
        String tierName = null;
        
        if (ENTERPRISE_TIER.equalsIgnoreCase(customerTier)) {
            discountPercent = enterpriseDiscountPercent;
            tierName = "Enterprise";
        } else if (PARTNER_TIER.equalsIgnoreCase(customerTier)) {
            discountPercent = partnerDiscountPercent;
            tierName = "Partner";
        }
        
        if (discountPercent != null) {
            BigDecimal currentTotal = currentResult.getSubtotal().subtract(currentResult.getTotalDiscount());
            BigDecimal tierDiscount = currentTotal
                    .multiply(discountPercent)
                    .setScale(2, RoundingMode.HALF_UP);
            
            currentResult.addOrderDiscount(tierDiscount);
            currentResult.addDiscountDescription(
                    String.format("%s tier discount: %d%% off - saved $%.2f",
                            tierName,
                            discountPercent.multiply(BigDecimal.valueOf(100)).intValue(),
                            tierDiscount));
            currentResult.addAppliedStrategy(getName());
        }
        
        return currentResult;
    }

    @Override
    public String getName() {
        return "PartnerDiscount";
    }

    @Override
    public int getOrder() {
        return 30;
    }
}

