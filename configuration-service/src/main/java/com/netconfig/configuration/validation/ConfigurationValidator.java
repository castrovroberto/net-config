package com.netconfig.configuration.validation;

import com.netconfig.configuration.client.CatalogClient;
import com.netconfig.configuration.client.dto.ProductResponse;
import com.netconfig.configuration.domain.ConfigurationItem;
import com.netconfig.configuration.domain.RackConfiguration;
import com.netconfig.configuration.validation.context.ValidationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Orchestrates configuration validation by running all rules in order.
 * Uses Chain of Responsibility pattern to execute validation rules.
 */
@Component
public class ConfigurationValidator {

    private static final Logger log = LoggerFactory.getLogger(ConfigurationValidator.class);

    private final List<ConfigurationRule> rules;
    private final CatalogClient catalogClient;

    public ConfigurationValidator(List<ConfigurationRule> rules, CatalogClient catalogClient) {
        // Sort rules by order
        this.rules = rules.stream()
                .sorted(Comparator.comparingInt(ConfigurationRule::getOrder))
                .toList();
        this.catalogClient = catalogClient;

        log.info("Initialized ConfigurationValidator with {} rules: {}", 
                rules.size(),
                rules.stream().map(ConfigurationRule::getRuleName).toList());
    }

    /**
     * Validate a configuration against all rules.
     *
     * @param configuration The rack configuration to validate
     * @return Complete validation result with all rule outcomes
     */
    public ValidationSummary validate(RackConfiguration configuration) {
        log.info("Validating configuration: {}", configuration.getId());

        // Build validation context with pre-loaded product data
        ValidationContext context = buildContext(configuration);

        // Execute all rules
        List<RuleResult> results = new ArrayList<>();
        boolean allPassed = true;

        for (ConfigurationRule rule : rules) {
            try {
                RuleResult result = rule.validate(context);
                results.add(result);

                if (!result.passed()) {
                    allPassed = false;
                    log.debug("Rule {} failed: {}", rule.getRuleName(), result.errors());
                } else if (!result.warnings().isEmpty()) {
                    log.debug("Rule {} passed with warnings: {}", rule.getRuleName(), result.warnings());
                }
            } catch (Exception e) {
                log.error("Rule {} threw exception", rule.getRuleName(), e);
                results.add(RuleResult.fail(rule.getRuleName(), 
                        "Internal error during validation: " + e.getMessage()));
                allPassed = false;
            }
        }

        ValidationSummary summary = new ValidationSummary(
                configuration.getId(),
                allPassed,
                results,
                context.getTotalPowerDraw(),
                context.getTotalPsuCapacity(),
                context.getTotalRackUnitsUsed(),
                context.getRackCapacity()
        );

        log.info("Validation complete for {}: {}", 
                configuration.getId(), 
                allPassed ? "PASSED" : "FAILED");

        return summary;
    }

    /**
     * Build validation context by fetching all required product data.
     */
    private ValidationContext buildContext(RackConfiguration configuration) {
        // Fetch rack product
        ProductResponse rack = null;
        if (configuration.getRackSku() != null && !configuration.getRackSku().isBlank()) {
            rack = catalogClient.getProductBySku(configuration.getRackSku()).orElse(null);
        }

        // Fetch all component products
        Map<String, ProductResponse> productsBySku = new HashMap<>();
        for (ConfigurationItem item : configuration.getItems()) {
            if (!productsBySku.containsKey(item.getProductSku())) {
                catalogClient.getProductBySku(item.getProductSku())
                        .ifPresent(product -> productsBySku.put(item.getProductSku(), product));
            }
        }

        return new ValidationContext(configuration, rack, productsBySku);
    }

    /**
     * Get the list of active validation rules.
     */
    public List<String> getRuleNames() {
        return rules.stream()
                .map(ConfigurationRule::getRuleName)
                .toList();
    }
}

