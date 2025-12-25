package com.netconfig.configuration.validation.rules;

import com.netconfig.configuration.validation.ConfigurationRule;
import com.netconfig.configuration.validation.RuleResult;
import com.netconfig.configuration.validation.context.ValidationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Validates that total power consumption does not exceed PSU capacity.
 * 
 * Rule: sum(component.power_draw) <= sum(psu.capacity_watts)
 */
@Component
@Order(10)
public class PowerBudgetRule implements ConfigurationRule {

    private static final double WARNING_THRESHOLD = 0.8; // Warn at 80% capacity

    @Override
    public RuleResult validate(ValidationContext context) {
        int totalPowerDraw = context.getTotalPowerDraw();
        int totalPsuCapacity = context.getTotalPsuCapacity();

        // If no powered components, skip this rule
        if (!context.hasPoweredComponents()) {
            return RuleResult.pass(getRuleName());
        }

        // Check if PSUs are configured
        if (totalPsuCapacity == 0) {
            return RuleResult.fail(getRuleName(),
                    String.format("Configuration requires %dW of power but no PSU is configured", 
                            totalPowerDraw));
        }

        // Check if power budget is exceeded
        if (totalPowerDraw > totalPsuCapacity) {
            return RuleResult.fail(getRuleName(),
                    String.format("Power budget exceeded: %dW required but only %dW available (deficit: %dW)",
                            totalPowerDraw, totalPsuCapacity, totalPowerDraw - totalPsuCapacity));
        }

        // Check for warning threshold
        double utilizationRatio = (double) totalPowerDraw / totalPsuCapacity;
        if (utilizationRatio >= WARNING_THRESHOLD) {
            int percentUsed = (int) (utilizationRatio * 100);
            return RuleResult.pass(getRuleName(), java.util.List.of(
                    String.format("Power utilization at %d%% (%dW of %dW) - consider additional PSU capacity",
                            percentUsed, totalPowerDraw, totalPsuCapacity)
            ));
        }

        return RuleResult.pass(getRuleName());
    }

    @Override
    public String getRuleName() {
        return "PowerBudget";
    }

    @Override
    public int getOrder() {
        return 10;
    }
}

