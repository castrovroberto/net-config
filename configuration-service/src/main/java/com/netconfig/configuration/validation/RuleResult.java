package com.netconfig.configuration.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result of a single validation rule.
 */
public record RuleResult(
    String ruleName,
    boolean passed,
    List<String> errors,
    List<String> warnings
) {
    public static RuleResult pass(String ruleName) {
        return new RuleResult(ruleName, true, Collections.emptyList(), Collections.emptyList());
    }

    public static RuleResult pass(String ruleName, List<String> warnings) {
        return new RuleResult(ruleName, true, Collections.emptyList(), warnings);
    }

    public static RuleResult fail(String ruleName, String error) {
        return new RuleResult(ruleName, false, List.of(error), Collections.emptyList());
    }

    public static RuleResult fail(String ruleName, List<String> errors) {
        return new RuleResult(ruleName, false, errors, Collections.emptyList());
    }

    public static RuleResult fail(String ruleName, List<String> errors, List<String> warnings) {
        return new RuleResult(ruleName, false, errors, warnings);
    }

    /**
     * Builder for constructing complex results.
     */
    public static Builder builder(String ruleName) {
        return new Builder(ruleName);
    }

    public static class Builder {
        private final String ruleName;
        private final List<String> errors = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();

        private Builder(String ruleName) {
            this.ruleName = ruleName;
        }

        public Builder addError(String error) {
            errors.add(error);
            return this;
        }

        public Builder addWarning(String warning) {
            warnings.add(warning);
            return this;
        }

        public RuleResult build() {
            return new RuleResult(ruleName, errors.isEmpty(), 
                    Collections.unmodifiableList(errors), 
                    Collections.unmodifiableList(warnings));
        }
    }
}

