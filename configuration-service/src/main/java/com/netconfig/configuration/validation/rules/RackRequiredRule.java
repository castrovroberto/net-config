package com.netconfig.configuration.validation.rules;

import com.netconfig.configuration.validation.ConfigurationRule;
import com.netconfig.configuration.validation.RuleResult;
import com.netconfig.configuration.validation.context.ValidationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Validates that a base rack is selected for the configuration.
 */
@Component
@Order(1)
public class RackRequiredRule implements ConfigurationRule {

    @Override
    public RuleResult validate(ValidationContext context) {
        if (context.getConfiguration().getRackSku() == null || 
            context.getConfiguration().getRackSku().isBlank()) {
            return RuleResult.fail(getRuleName(), 
                    "A base rack must be selected for the configuration");
        }

        if (context.getRack() == null) {
            return RuleResult.fail(getRuleName(), 
                    "Selected rack SKU is not valid: " + context.getConfiguration().getRackSku());
        }

        return RuleResult.pass(getRuleName());
    }

    @Override
    public String getRuleName() {
        return "RackRequired";
    }

    @Override
    public int getOrder() {
        return 1;
    }
}

