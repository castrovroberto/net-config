package com.netconfig.configuration.validation.rules;

import com.netconfig.configuration.validation.ConfigurationRule;
import com.netconfig.configuration.validation.RuleResult;
import com.netconfig.configuration.validation.context.ValidationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Validates that at least one PSU is configured if there are powered components.
 */
@Component
@Order(5)
public class MinimumPsuRule implements ConfigurationRule {

    @Override
    public RuleResult validate(ValidationContext context) {
        // If there are powered components, at least one PSU is required
        if (context.hasPoweredComponents()) {
            int psuCount = context.getCountByType("PSU");
            
            if (psuCount == 0) {
                int switchCount = context.getCountByType("SWITCH");
                return RuleResult.fail(getRuleName(),
                        String.format("Configuration has %d switch(es) that require power, but no PSU is configured",
                                switchCount));
            }
        }

        return RuleResult.pass(getRuleName());
    }

    @Override
    public String getRuleName() {
        return "MinimumPSU";
    }

    @Override
    public int getOrder() {
        return 5;
    }
}

