package com.netconfig.pricing.strategy.impl;

import com.netconfig.pricing.domain.PricingContext;
import com.netconfig.pricing.domain.PricingLineItem;
import com.netconfig.pricing.domain.PricingResult;
import com.netconfig.pricing.strategy.PricingStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Volume discount strategy: Apply discount when switch count exceeds threshold.
 * Default: 10% off switches when buying more than 5.
 */
@Component
@Order(10)
public class VolumeDiscountStrategy implements PricingStrategy {

    private final int switchThreshold;
    private final BigDecimal discountPercent;

    public VolumeDiscountStrategy(
            @Value("${pricing.volume-discount.switch-threshold:5}") int switchThreshold,
            @Value("${pricing.volume-discount.discount-percent:10}") int discountPercent) {
        this.switchThreshold = switchThreshold;
        this.discountPercent = BigDecimal.valueOf(discountPercent).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
    }

    @Override
    public PricingResult apply(PricingContext context, PricingResult currentResult) {
        int switchCount = context.getSwitchCount();
        
        if (switchCount > switchThreshold) {
            // Apply discount to all switch line items
            BigDecimal totalSwitchDiscount = BigDecimal.ZERO;
            
            for (PricingLineItem item : currentResult.getLineItems()) {
                if ("SWITCH".equals(item.getProductType())) {
                    BigDecimal itemDiscount = item.getLineTotal()
                            .multiply(discountPercent)
                            .setScale(2, RoundingMode.HALF_UP);
                    
                    item.setDiscountAmount(item.getDiscountAmount().add(itemDiscount));
                    item.setDiscountReason(String.format("Volume discount: %d%% off (>%d switches)",
                            discountPercent.multiply(BigDecimal.valueOf(100)).intValue(),
                            switchThreshold));
                    
                    totalSwitchDiscount = totalSwitchDiscount.add(itemDiscount);
                }
            }
            
            if (totalSwitchDiscount.compareTo(BigDecimal.ZERO) > 0) {
                currentResult.setTotalDiscount(
                        currentResult.getTotalDiscount().add(totalSwitchDiscount));
                currentResult.addDiscountDescription(
                        String.format("Volume discount: %d%% off switches (purchased %d, threshold %d) - saved $%.2f",
                                discountPercent.multiply(BigDecimal.valueOf(100)).intValue(),
                                switchCount,
                                switchThreshold,
                                totalSwitchDiscount));
                currentResult.addAppliedStrategy(getName());
                currentResult.recalculateTotals();
            }
        }
        
        return currentResult;
    }

    @Override
    public String getName() {
        return "VolumeDiscount";
    }

    @Override
    public int getOrder() {
        return 10;
    }
}

