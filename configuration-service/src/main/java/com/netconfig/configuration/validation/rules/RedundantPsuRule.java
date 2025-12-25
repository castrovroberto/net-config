package com.netconfig.configuration.validation.rules;

import com.netconfig.configuration.validation.ConfigurationRule;
import com.netconfig.configuration.validation.RuleResult;
import com.netconfig.configuration.validation.context.ValidationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Recommends redundant PSU configuration for high availability.
 * This is a warning-only rule (non-blocking).
 */
@Component
@Order(30)
public class RedundantPsuRule implements ConfigurationRule {

    private static final int HIGH_VALUE_THRESHOLD_SWITCHES = 3;

    @Override
    public RuleResult validate(ValidationContext context) {
        int psuCount = context.getCountByType("PSU");
        int switchCount = context.getCountByType("SWITCH");

        List<String> warnings = new ArrayList<>();

        // Recommend redundant PSU for configurations with multiple switches
        if (switchCount >= HIGH_VALUE_THRESHOLD_SWITCHES && psuCount < 2) {
            warnings.add(String.format(
                    "Configuration has %d switches but only %d PSU. " +
                    "Consider adding a redundant PSU for high availability.",
                    switchCount, psuCount));
        }

        // Check if single PSU is a single point of failure
        if (psuCount == 1 && context.hasPoweredComponents()) {
            int powerDraw = context.getTotalPowerDraw();
            int psuCapacity = context.getTotalPsuCapacity();
            
            // If using more than 50% of single PSU, recommend redundancy
            if (powerDraw > psuCapacity * 0.5) {
                warnings.add(String.format(
                        "Single PSU at %d%% utilization. " +
                        "Redundant PSU recommended for fault tolerance.",
                        (int) ((double) powerDraw / psuCapacity * 100)));
            }
        }

        return warnings.isEmpty() 
                ? RuleResult.pass(getRuleName()) 
                : RuleResult.pass(getRuleName(), warnings);
    }

    @Override
    public String getRuleName() {
        return "RedundantPSU";
    }

    @Override
    public int getOrder() {
        return 30;
    }
}

