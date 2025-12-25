package com.netconfig.configuration.validation;

import com.netconfig.configuration.validation.context.ValidationContext;

/**
 * Interface for configuration validation rules.
 * Implements the Chain of Responsibility pattern.
 */
public interface ConfigurationRule {

    /**
     * Validate the configuration.
     *
     * @param context The validation context containing configuration and product data
     * @return The validation result
     */
    RuleResult validate(ValidationContext context);

    /**
     * Get the name of this rule for logging and reporting.
     */
    String getRuleName();

    /**
     * Get the order in which this rule should be executed.
     * Lower numbers execute first.
     */
    default int getOrder() {
        return 100;
    }
}

