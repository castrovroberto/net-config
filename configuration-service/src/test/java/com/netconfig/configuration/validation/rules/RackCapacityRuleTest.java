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
 * Unit tests for RackCapacityRule.
 */
class RackCapacityRuleTest {

    private RackCapacityRule rule;

    @BeforeEach
    void setUp() {
        rule = new RackCapacityRule();
    }

    @Test
    @DisplayName("Should pass when rack units used is within capacity")
    void shouldPassWhenWithinCapacity() {
        // Given: 24U rack with 10 x 1U switches = 10U used
        RackConfiguration config = createConfiguration("RACK-24U");
        config.addItem(createItem("SW-1", 10));

        Map<String, ProductResponse> products = new HashMap<>();
        products.put("SW-1", createSwitch("SW-1", 1));

        ValidationContext context = new ValidationContext(
                config,
                createRack("RACK-24U", 24),
                products
        );

        // When
        RuleResult result = rule.validate(context);

        // Then
        assertThat(result.passed()).isTrue();
        assertThat(result.errors()).isEmpty();
    }

    @Test
    @DisplayName("Should fail when rack units exceed capacity")
    void shouldFailWhenCapacityExceeded() {
        // Given: 24U rack with 26 x 1U switches = 26U used (exceeds 24U)
        RackConfiguration config = createConfiguration("RACK-24U");
        config.addItem(createItem("SW-1", 26));

        Map<String, ProductResponse> products = new HashMap<>();
        products.put("SW-1", createSwitch("SW-1", 1));

        ValidationContext context = new ValidationContext(
                config,
                createRack("RACK-24U", 24),
                products
        );

        // When
        RuleResult result = rule.validate(context);

        // Then
        assertThat(result.passed()).isFalse();
        assertThat(result.errors()).hasSize(1);
        assertThat(result.errors().get(0)).contains("Rack capacity exceeded");
        assertThat(result.errors().get(0)).contains("26U required");
        assertThat(result.errors().get(0)).contains("24U");
    }

    @Test
    @DisplayName("Should warn when rack utilization is above 90%")
    void shouldWarnWhenHighUtilization() {
        // Given: 24U rack with 22 x 1U switches = 92% utilization
        RackConfiguration config = createConfiguration("RACK-24U");
        config.addItem(createItem("SW-1", 22));

        Map<String, ProductResponse> products = new HashMap<>();
        products.put("SW-1", createSwitch("SW-1", 1));

        ValidationContext context = new ValidationContext(
                config,
                createRack("RACK-24U", 24),
                products
        );

        // When
        RuleResult result = rule.validate(context);

        // Then
        assertThat(result.passed()).isTrue();
        assertThat(result.warnings()).hasSize(1);
        assertThat(result.warnings().get(0)).contains("91%");
    }

    @Test
    @DisplayName("Should handle 2U devices correctly")
    void shouldHandle2UDevices() {
        // Given: 24U rack with 5 x 2U switches and 10 x 1U switches = 20U
        RackConfiguration config = createConfiguration("RACK-24U");
        config.addItem(createItem("SW-2U", 5));
        config.addItem(createItem("SW-1U", 10));

        Map<String, ProductResponse> products = new HashMap<>();
        products.put("SW-2U", createSwitch("SW-2U", 2));
        products.put("SW-1U", createSwitch("SW-1U", 1));

        ValidationContext context = new ValidationContext(
                config,
                createRack("RACK-24U", 24),
                products
        );

        // When
        RuleResult result = rule.validate(context);

        // Then
        assertThat(result.passed()).isTrue();
        // 20U used in 24U rack = 83% - no warning
        assertThat(result.warnings()).isEmpty();
    }

    @Test
    @DisplayName("Should ignore 0U components (vertical mount)")
    void shouldIgnoreZeroUnitComponents() {
        // Given: 24U rack with vertical-mount PSU (0U) and 24 x 1U switches
        RackConfiguration config = createConfiguration("RACK-24U");
        config.addItem(createItem("SW-1", 24));
        config.addItem(createItem("PSU-VERT", 2));

        Map<String, ProductResponse> products = new HashMap<>();
        products.put("SW-1", createSwitch("SW-1", 1));
        products.put("PSU-VERT", createVerticalPsu("PSU-VERT"));

        ValidationContext context = new ValidationContext(
                config,
                createRack("RACK-24U", 24),
                products
        );

        // When
        RuleResult result = rule.validate(context);

        // Then
        assertThat(result.passed()).isTrue();
        // Exactly at capacity, should warn
        assertThat(result.warnings()).hasSize(1);
    }

    // Helper methods
    private RackConfiguration createConfiguration(String rackSku) {
        RackConfiguration config = new RackConfiguration();
        config.setId("test-config-1");
        config.setRackSku(rackSku);
        return config;
    }

    private ConfigurationItem createItem(String sku, int quantity) {
        ConfigurationItem item = new ConfigurationItem();
        item.setProductSku(sku);
        item.setQuantity(quantity);
        return item;
    }

    private ProductResponse createRack(String sku, int units) {
        return new ProductResponse(
                "rack-" + sku, sku, sku + " Rack", "RACK", 2499.99,
                Map.of("units", units),
                Map.of()
        );
    }

    private ProductResponse createSwitch(String sku, int rackUnits) {
        return new ProductResponse(
                "sw-" + sku, sku, "Switch " + sku, "SWITCH", 4599.99,
                Map.of("power_draw", 350, "rack_units", rackUnits),
                Map.of("requires_power", true)
        );
    }

    private ProductResponse createVerticalPsu(String sku) {
        return new ProductResponse(
                "psu-" + sku, sku, "Vertical PSU " + sku, "PSU", 2499.99,
                Map.of("capacity_watts", 3000, "rack_units", 0),
                Map.of()
        );
    }
}

