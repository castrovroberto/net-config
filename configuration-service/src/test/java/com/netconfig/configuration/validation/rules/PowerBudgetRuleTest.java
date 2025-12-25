package com.netconfig.configuration.validation.rules;

import com.netconfig.configuration.client.dto.ProductResponse;
import com.netconfig.configuration.domain.ConfigurationItem;
import com.netconfig.configuration.domain.RackConfiguration;
import com.netconfig.configuration.validation.RuleResult;
import com.netconfig.configuration.validation.context.ValidationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for PowerBudgetRule.
 */
class PowerBudgetRuleTest {

    private PowerBudgetRule rule;

    @BeforeEach
    void setUp() {
        rule = new PowerBudgetRule();
    }

    @Test
    @DisplayName("Should pass when power draw is within PSU capacity")
    void shouldPassWhenPowerWithinCapacity() {
        // Given: 2 switches at 350W each = 700W, 1 PSU at 1000W
        RackConfiguration config = createConfiguration();
        config.addItem(createItem("SW-1", 2));
        config.addItem(createItem("PSU-1", 1));

        Map<String, ProductResponse> products = new HashMap<>();
        products.put("SW-1", createSwitch("SW-1", 350));
        products.put("PSU-1", createPsu("PSU-1", 1000));

        ValidationContext context = new ValidationContext(
                config,
                createRack(),
                products
        );

        // When
        RuleResult result = rule.validate(context);

        // Then
        assertThat(result.passed()).isTrue();
        assertThat(result.errors()).isEmpty();
    }

    @Test
    @DisplayName("Should fail when power draw exceeds PSU capacity")
    void shouldFailWhenPowerExceedsCapacity() {
        // Given: 3 switches at 350W each = 1050W, 1 PSU at 1000W
        RackConfiguration config = createConfiguration();
        config.addItem(createItem("SW-1", 3));
        config.addItem(createItem("PSU-1", 1));

        Map<String, ProductResponse> products = new HashMap<>();
        products.put("SW-1", createSwitch("SW-1", 350));
        products.put("PSU-1", createPsu("PSU-1", 1000));

        ValidationContext context = new ValidationContext(
                config,
                createRack(),
                products
        );

        // When
        RuleResult result = rule.validate(context);

        // Then
        assertThat(result.passed()).isFalse();
        assertThat(result.errors()).hasSize(1);
        assertThat(result.errors().get(0)).contains("Power budget exceeded");
        assertThat(result.errors().get(0)).contains("1050W required");
        assertThat(result.errors().get(0)).contains("1000W available");
    }

    @Test
    @DisplayName("Should fail when no PSU is configured but switches exist")
    void shouldFailWhenNoPsuConfigured() {
        // Given: 2 switches but no PSU
        RackConfiguration config = createConfiguration();
        config.addItem(createItem("SW-1", 2));

        Map<String, ProductResponse> products = new HashMap<>();
        products.put("SW-1", createSwitch("SW-1", 350));

        ValidationContext context = new ValidationContext(
                config,
                createRack(),
                products
        );

        // When
        RuleResult result = rule.validate(context);

        // Then
        assertThat(result.passed()).isFalse();
        assertThat(result.errors().get(0)).contains("no PSU is configured");
    }

    @Test
    @DisplayName("Should warn when power utilization is above 80%")
    void shouldWarnWhenHighUtilization() {
        // Given: 850W draw on 1000W PSU (85%)
        RackConfiguration config = createConfiguration();
        config.addItem(createItem("SW-1", 1));
        config.addItem(createItem("PSU-1", 1));

        Map<String, ProductResponse> products = new HashMap<>();
        products.put("SW-1", createSwitch("SW-1", 850));
        products.put("PSU-1", createPsu("PSU-1", 1000));

        ValidationContext context = new ValidationContext(
                config,
                createRack(),
                products
        );

        // When
        RuleResult result = rule.validate(context);

        // Then
        assertThat(result.passed()).isTrue();
        assertThat(result.warnings()).hasSize(1);
        assertThat(result.warnings().get(0)).contains("85%");
    }

    @Test
    @DisplayName("Should pass without warnings when no powered components")
    void shouldPassWhenNoPoweredComponents() {
        // Given: Only cables (no power draw)
        RackConfiguration config = createConfiguration();
        config.addItem(createItem("CABLE-1", 5));

        Map<String, ProductResponse> products = new HashMap<>();
        products.put("CABLE-1", createCable("CABLE-1"));

        ValidationContext context = new ValidationContext(
                config,
                createRack(),
                products
        );

        // When
        RuleResult result = rule.validate(context);

        // Then
        assertThat(result.passed()).isTrue();
        assertThat(result.warnings()).isEmpty();
    }

    // Helper methods
    private RackConfiguration createConfiguration() {
        RackConfiguration config = new RackConfiguration();
        config.setId("test-config-1");
        config.setRackSku("RACK-42U");
        return config;
    }

    private ConfigurationItem createItem(String sku, int quantity) {
        ConfigurationItem item = new ConfigurationItem();
        item.setProductSku(sku);
        item.setQuantity(quantity);
        return item;
    }

    private ProductResponse createRack() {
        return new ProductResponse(
                "rack-1", "RACK-42U", "42U Rack", "RACK", 2499.99,
                Map.of("units", 42),
                Map.of()
        );
    }

    private ProductResponse createSwitch(String sku, int powerDraw) {
        return new ProductResponse(
                "sw-" + sku, sku, "Switch " + sku, "SWITCH", 4599.99,
                Map.of("power_draw", powerDraw, "rack_units", 1),
                Map.of("requires_power", true)
        );
    }

    private ProductResponse createPsu(String sku, int capacity) {
        return new ProductResponse(
                "psu-" + sku, sku, "PSU " + sku, "PSU", 599.99,
                Map.of("capacity_watts", capacity, "rack_units", 1),
                Map.of()
        );
    }

    private ProductResponse createCable(String sku) {
        return new ProductResponse(
                "cable-" + sku, sku, "Cable " + sku, "CABLE", 49.99,
                Map.of(),
                Map.of()
        );
    }
}

