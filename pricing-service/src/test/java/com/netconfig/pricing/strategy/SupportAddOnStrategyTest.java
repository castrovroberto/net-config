package com.netconfig.pricing.strategy;

import com.netconfig.pricing.domain.PricingContext;
import com.netconfig.pricing.domain.PricingLineItem;
import com.netconfig.pricing.domain.PricingResult;
import com.netconfig.pricing.strategy.impl.SupportAddOnStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for SupportAddOnStrategy.
 */
class SupportAddOnStrategyTest {

    private SupportAddOnStrategy strategy;

    @BeforeEach
    void setUp() {
        // Standard: 15%, Premium: 20%
        strategy = new SupportAddOnStrategy(15, 20);
    }

    @Test
    @DisplayName("Should add standard support cost (15%)")
    void shouldAddStandardSupportCost() {
        // Given: $10000 hardware, standard support requested
        List<PricingLineItem> items = new ArrayList<>();
        items.add(createItem("SW-1", 5, new BigDecimal("2000.00")));
        
        Map<String, Object> options = new HashMap<>();
        options.put("include_support", true);
        options.put("support_tier", "STANDARD");
        
        PricingContext context = new PricingContext("config-1", items);
        context.setOptions(options);
        
        PricingResult result = createInitialResult(items);

        // When
        result = strategy.apply(context, result);

        // Then: 15% of $10000 = $1500
        assertThat(result.getServiceAddOn()).isEqualByComparingTo(new BigDecimal("1500.00"));
        assertThat(result.getAppliedStrategies()).contains("SupportAddOn");
    }

    @Test
    @DisplayName("Should add premium support cost (20%)")
    void shouldAddPremiumSupportCost() {
        // Given: $10000 hardware, premium support requested
        List<PricingLineItem> items = new ArrayList<>();
        items.add(createItem("SW-1", 5, new BigDecimal("2000.00")));
        
        Map<String, Object> options = new HashMap<>();
        options.put("include_support", true);
        options.put("support_tier", "PREMIUM");
        
        PricingContext context = new PricingContext("config-1", items);
        context.setOptions(options);
        
        PricingResult result = createInitialResult(items);

        // When
        result = strategy.apply(context, result);

        // Then: 20% of $10000 = $2000
        assertThat(result.getServiceAddOn()).isEqualByComparingTo(new BigDecimal("2000.00"));
    }

    @Test
    @DisplayName("Should not add support cost when not requested")
    void shouldNotAddSupportWhenNotRequested() {
        // Given: Support not requested
        List<PricingLineItem> items = new ArrayList<>();
        items.add(createItem("SW-1", 5, new BigDecimal("2000.00")));
        
        PricingContext context = new PricingContext("config-1", items);
        // No support option set
        
        PricingResult result = createInitialResult(items);

        // When
        result = strategy.apply(context, result);

        // Then: No support cost
        assertThat(result.getServiceAddOn()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getAppliedStrategies()).doesNotContain("SupportAddOn");
    }

    @Test
    @DisplayName("Should calculate support on discounted total")
    void shouldCalculateOnDiscountedTotal() {
        // Given: $10000 hardware with $2000 order discount
        List<PricingLineItem> items = new ArrayList<>();
        items.add(createItem("SW-1", 5, new BigDecimal("2000.00")));
        
        Map<String, Object> options = new HashMap<>();
        options.put("include_support", true);
        options.put("support_tier", "STANDARD");
        
        PricingContext context = new PricingContext("config-1", items);
        context.setOptions(options);
        
        PricingResult result = createInitialResult(items);
        result.addOrderDiscount(new BigDecimal("2000.00"));  // $2000 order discount

        // When
        result = strategy.apply(context, result);

        // Then: 15% of ($10000 - $2000) = 15% of $8000 = $1200
        assertThat(result.getServiceAddOn()).isEqualByComparingTo(new BigDecimal("1200.00"));
    }

    @Test
    @DisplayName("Should default to standard tier when tier not specified")
    void shouldDefaultToStandardTier() {
        // Given: Support requested but tier not specified
        List<PricingLineItem> items = new ArrayList<>();
        items.add(createItem("SW-1", 1, new BigDecimal("10000.00")));
        
        Map<String, Object> options = new HashMap<>();
        options.put("include_support", true);
        // No support_tier specified
        
        PricingContext context = new PricingContext("config-1", items);
        context.setOptions(options);
        
        PricingResult result = createInitialResult(items);

        // When
        result = strategy.apply(context, result);

        // Then: Should use standard 15%
        assertThat(result.getServiceAddOn()).isEqualByComparingTo(new BigDecimal("1500.00"));
    }

    // Helper methods
    private PricingLineItem createItem(String sku, int quantity, BigDecimal unitPrice) {
        return new PricingLineItem(sku, "Item " + sku, "SWITCH", quantity, unitPrice);
    }

    private PricingResult createInitialResult(List<PricingLineItem> items) {
        PricingResult result = new PricingResult("config-1");
        result.setLineItems(new ArrayList<>(items));
        result.recalculateTotals();  // This properly initializes all totals
        return result;
    }
}

