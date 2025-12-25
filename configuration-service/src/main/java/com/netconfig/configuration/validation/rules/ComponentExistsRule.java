package com.netconfig.configuration.validation.rules;

import com.netconfig.configuration.domain.ConfigurationItem;
import com.netconfig.configuration.validation.ConfigurationRule;
import com.netconfig.configuration.validation.RuleResult;
import com.netconfig.configuration.validation.context.ValidationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates that all products referenced in the configuration exist in the catalog.
 */
@Component
@Order(2)
public class ComponentExistsRule implements ConfigurationRule {

    @Override
    public RuleResult validate(ValidationContext context) {
        List<String> errors = new ArrayList<>();

        for (ConfigurationItem item : context.getAllItems()) {
            if (context.getProduct(item.getProductSku()).isEmpty()) {
                errors.add(String.format("Product not found in catalog: %s", item.getProductSku()));
            }
        }

        if (!errors.isEmpty()) {
            return RuleResult.fail(getRuleName(), errors);
        }

        return RuleResult.pass(getRuleName());
    }

    @Override
    public String getRuleName() {
        return "ComponentExists";
    }

    @Override
    public int getOrder() {
        return 2;
    }
}

