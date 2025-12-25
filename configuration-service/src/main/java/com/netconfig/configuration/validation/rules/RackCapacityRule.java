package com.netconfig.configuration.validation.rules;

import com.netconfig.configuration.validation.ConfigurationRule;
import com.netconfig.configuration.validation.RuleResult;
import com.netconfig.configuration.validation.context.ValidationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Validates that total rack units used does not exceed rack capacity.
 * 
 * Rule: sum(component.rack_units) <= rack.units
 */
@Component
@Order(20)
public class RackCapacityRule implements ConfigurationRule {

    private static final double WARNING_THRESHOLD = 0.9; // Warn at 90% capacity

    @Override
    public RuleResult validate(ValidationContext context) {
        // Skip if no rack is configured (RackRequiredRule will catch this)
        if (context.getRack() == null) {
            return RuleResult.pass(getRuleName());
        }

        int totalUnitsUsed = context.getTotalRackUnitsUsed();
        int rackCapacity = context.getRackCapacity();

        if (rackCapacity == 0) {
            return RuleResult.fail(getRuleName(), 
                    "Rack capacity information is not available");
        }

        // Check if capacity is exceeded
        if (totalUnitsUsed > rackCapacity) {
            return RuleResult.fail(getRuleName(),
                    String.format("Rack capacity exceeded: %dU required but rack only has %dU (excess: %dU)",
                            totalUnitsUsed, rackCapacity, totalUnitsUsed - rackCapacity));
        }

        // Check for warning threshold
        double utilizationRatio = (double) totalUnitsUsed / rackCapacity;
        if (utilizationRatio >= WARNING_THRESHOLD) {
            int percentUsed = (int) (utilizationRatio * 100);
            return RuleResult.pass(getRuleName(), java.util.List.of(
                    String.format("Rack utilization at %d%% (%dU of %dU) - limited space for expansion",
                            percentUsed, totalUnitsUsed, rackCapacity)
            ));
        }

        return RuleResult.pass(getRuleName());
    }

    @Override
    public String getRuleName() {
        return "RackCapacity";
    }

    @Override
    public int getOrder() {
        return 20;
    }
}

