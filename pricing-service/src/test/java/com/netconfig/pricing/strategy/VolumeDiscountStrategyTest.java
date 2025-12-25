package com.netconfig.pricing.strategy;

import com.netconfig.pricing.domain.PricingContext;
import com.netconfig.pricing.domain.PricingLineItem;
import com.netconfig.pricing.domain.PricingResult;
import com.netconfig.pricing.strategy.impl.VolumeDiscountStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for VolumeDiscountStrategy.
 */
class VolumeDiscountStrategyTest {

    private VolumeDiscountStrategy strategy;

    @BeforeEach
    void setUp() {
        // Threshold: 5 switches, Discount: 10%
        strategy = new VolumeDiscountStrategy(5, 10);
    }

    @Test
    @DisplayName("Should apply 10% discount when more than 5 switches")
    void shouldApplyDiscountWhenAboveThreshold() {
        // Given: 6 switches at $1000 each = $6000
        List<PricingLineItem> items = new ArrayList<>();
        items.add(createSwitchItem("SW-1", 6, new BigDecimal("1000.00")));
        
        PricingContext context = new PricingContext("config-1", items);
        PricingResult result = createInitialResult(items);

        // When
        result = strategy.apply(context, result);

        // Then: 10% of $6000 = $600 discount
        assertThat(result.getTotalDiscount()).isEqualByComparingTo(new BigDecimal("600.00"));
        assertThat(result.getAppliedStrategies()).contains("VolumeDiscount");
        assertThat(result.getDiscountDescriptions()).hasSize(1);
        assertThat(result.getDiscountDescriptions().get(0)).contains("10%");
    }

    @Test
    @DisplayName("Should not apply discount when exactly at threshold")
    void shouldNotApplyDiscountAtThreshold() {
        // Given: Exactly 5 switches
        List<PricingLineItem> items = new ArrayList<>();
        items.add(createSwitchItem("SW-1", 5, new BigDecimal("1000.00")));
        
        PricingContext context = new PricingContext("config-1", items);
        PricingResult result = createInitialResult(items);

        // When
        result = strategy.apply(context, result);

        // Then: No discount (must be MORE than 5)
        assertThat(result.getTotalDiscount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getAppliedStrategies()).doesNotContain("VolumeDiscount");
    }

    @Test
    @DisplayName("Should not apply discount when below threshold")
    void shouldNotApplyDiscountBelowThreshold() {
        // Given: 3 switches
        List<PricingLineItem> items = new ArrayList<>();
        items.add(createSwitchItem("SW-1", 3, new BigDecimal("1000.00")));
        
        PricingContext context = new PricingContext("config-1", items);
        PricingResult result = createInitialResult(items);

        // When
        result = strategy.apply(context, result);

        // Then: No discount
        assertThat(result.getTotalDiscount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should only discount switches, not other products")
    void shouldOnlyDiscountSwitches() {
        // Given: 6 switches + 2 PSUs
        List<PricingLineItem> items = new ArrayList<>();
        items.add(createSwitchItem("SW-1", 6, new BigDecimal("1000.00")));  // $6000
        items.add(createPsuItem("PSU-1", 2, new BigDecimal("500.00")));      // $1000
        
        PricingContext context = new PricingContext("config-1", items);
        PricingResult result = createInitialResult(items);

        // When
        result = strategy.apply(context, result);

        // Then: 10% of $6000 switches only = $600 (not $700)
        assertThat(result.getTotalDiscount()).isEqualByComparingTo(new BigDecimal("600.00"));
    }

    @Test
    @DisplayName("Should handle multiple switch line items")
    void shouldHandleMultipleSwitchItems() {
        // Given: 3 of type A + 4 of type B = 7 switches total
        List<PricingLineItem> items = new ArrayList<>();
        items.add(createSwitchItem("SW-A", 3, new BigDecimal("1000.00")));  // $3000
        items.add(createSwitchItem("SW-B", 4, new BigDecimal("2000.00")));  // $8000
        
        PricingContext context = new PricingContext("config-1", items);
        PricingResult result = createInitialResult(items);

        // When
        result = strategy.apply(context, result);

        // Then: 10% of $11000 = $1100
        assertThat(result.getTotalDiscount()).isEqualByComparingTo(new BigDecimal("1100.00"));
    }

    // Helper methods
    private PricingLineItem createSwitchItem(String sku, int quantity, BigDecimal unitPrice) {
        return new PricingLineItem(sku, "Switch " + sku, "SWITCH", quantity, unitPrice);
    }

    private PricingLineItem createPsuItem(String sku, int quantity, BigDecimal unitPrice) {
        return new PricingLineItem(sku, "PSU " + sku, "PSU", quantity, unitPrice);
    }

    private PricingResult createInitialResult(List<PricingLineItem> items) {
        PricingResult result = new PricingResult("config-1");
        result.setLineItems(new ArrayList<>(items));
        result.recalculateTotals();  // This properly initializes all totals
        return result;
    }
}

