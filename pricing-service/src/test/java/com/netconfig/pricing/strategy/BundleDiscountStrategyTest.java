package com.netconfig.pricing.strategy;

import com.netconfig.pricing.domain.PricingContext;
import com.netconfig.pricing.domain.PricingLineItem;
import com.netconfig.pricing.domain.PricingResult;
import com.netconfig.pricing.strategy.impl.BundleDiscountStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for BundleDiscountStrategy.
 */
class BundleDiscountStrategyTest {

    private BundleDiscountStrategy strategy;

    @BeforeEach
    void setUp() {
        // Threshold: 80% rack capacity, Discount: 5%
        strategy = new BundleDiscountStrategy(80, 5);
    }

    @Test
    @DisplayName("Should apply 5% discount when rack utilization >= 80%")
    void shouldApplyDiscountWhenHighUtilization() {
        // Given: 85% rack utilization, $10000 subtotal
        List<PricingLineItem> items = new ArrayList<>();
        items.add(createItem("SW-1", 5, new BigDecimal("2000.00")));  // $10000
        
        PricingContext context = new PricingContext("config-1", items);
        context.setRackUnitsUsed(34);  // 34 of 40 = 85%
        context.setRackCapacity(40);
        
        PricingResult result = createInitialResult(items);

        // When
        result = strategy.apply(context, result);

        // Then: 5% of $10000 = $500
        assertThat(result.getOrderDiscount()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(result.getTotalDiscount()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(result.getAppliedStrategies()).contains("BundleDiscount");
        assertThat(result.getDiscountDescriptions().get(0)).contains("85%");
    }

    @Test
    @DisplayName("Should apply discount at exactly 80% utilization")
    void shouldApplyDiscountAtExactThreshold() {
        // Given: Exactly 80% utilization
        List<PricingLineItem> items = new ArrayList<>();
        items.add(createItem("SW-1", 1, new BigDecimal("1000.00")));
        
        PricingContext context = new PricingContext("config-1", items);
        context.setRackUnitsUsed(32);  // 32 of 40 = 80%
        context.setRackCapacity(40);
        
        PricingResult result = createInitialResult(items);

        // When
        result = strategy.apply(context, result);

        // Then: Discount should be applied
        assertThat(result.getOrderDiscount()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(result.getTotalDiscount()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(result.getAppliedStrategies()).contains("BundleDiscount");
    }

    @Test
    @DisplayName("Should not apply discount when below threshold")
    void shouldNotApplyDiscountBelowThreshold() {
        // Given: 70% utilization
        List<PricingLineItem> items = new ArrayList<>();
        items.add(createItem("SW-1", 1, new BigDecimal("1000.00")));
        
        PricingContext context = new PricingContext("config-1", items);
        context.setRackUnitsUsed(28);  // 28 of 40 = 70%
        context.setRackCapacity(40);
        
        PricingResult result = createInitialResult(items);

        // When
        result = strategy.apply(context, result);

        // Then: No discount
        assertThat(result.getTotalDiscount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getAppliedStrategies()).doesNotContain("BundleDiscount");
    }

    @Test
    @DisplayName("Should not apply discount when rack info is missing")
    void shouldNotApplyDiscountWhenNoRackInfo() {
        // Given: No rack utilization info
        List<PricingLineItem> items = new ArrayList<>();
        items.add(createItem("SW-1", 1, new BigDecimal("1000.00")));
        
        PricingContext context = new PricingContext("config-1", items);
        // No rack info set
        
        PricingResult result = createInitialResult(items);

        // When
        result = strategy.apply(context, result);

        // Then: No discount
        assertThat(result.getTotalDiscount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should apply discount on remaining total after prior discounts")
    void shouldApplyOnRemainingTotal() {
        // Given: $10000 subtotal with $1000 prior line item discount
        List<PricingLineItem> items = new ArrayList<>();
        PricingLineItem item = createItem("SW-1", 5, new BigDecimal("2000.00"));
        item.setDiscountAmount(new BigDecimal("1000.00"));  // Prior line item discount
        items.add(item);
        
        PricingContext context = new PricingContext("config-1", items);
        context.setRackUnitsUsed(36);  // 90%
        context.setRackCapacity(40);
        
        PricingResult result = createInitialResult(items);

        // When
        result = strategy.apply(context, result);

        // Then: 5% of ($10000 - $1000) = 5% of $9000 = $450
        // Total discount = $1000 (line item) + $450 (order) = $1450
        assertThat(result.getTotalDiscount()).isEqualByComparingTo(new BigDecimal("1450.00"));
        assertThat(result.getOrderDiscount()).isEqualByComparingTo(new BigDecimal("450.00"));
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

