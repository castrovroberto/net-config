package com.netconfig.configuration.validation;

import java.time.Instant;
import java.util.List;

/**
 * Complete validation summary including all rule results and metrics.
 */
public record ValidationSummary(
    String configurationId,
    boolean valid,
    List<RuleResult> ruleResults,
    int totalPowerDrawWatts,
    int totalPsuCapacityWatts,
    int totalRackUnitsUsed,
    int rackCapacityUnits,
    Instant validatedAt
) {
    public ValidationSummary(
            String configurationId,
            boolean valid,
            List<RuleResult> ruleResults,
            int totalPowerDrawWatts,
            int totalPsuCapacityWatts,
            int totalRackUnitsUsed,
            int rackCapacityUnits) {
        this(configurationId, valid, ruleResults, totalPowerDrawWatts, 
             totalPsuCapacityWatts, totalRackUnitsUsed, rackCapacityUnits, Instant.now());
    }

    /**
     * Get all errors from all failed rules.
     */
    public List<String> getAllErrors() {
        return ruleResults.stream()
                .filter(r -> !r.passed())
                .flatMap(r -> r.errors().stream())
                .toList();
    }

    /**
     * Get all warnings from all rules.
     */
    public List<String> getAllWarnings() {
        return ruleResults.stream()
                .flatMap(r -> r.warnings().stream())
                .toList();
    }

    /**
     * Get names of failed rules.
     */
    public List<String> getFailedRules() {
        return ruleResults.stream()
                .filter(r -> !r.passed())
                .map(RuleResult::ruleName)
                .toList();
    }

    /**
     * Get power utilization percentage.
     */
    public int getPowerUtilizationPercent() {
        if (totalPsuCapacityWatts == 0) return 0;
        return (int) ((double) totalPowerDrawWatts / totalPsuCapacityWatts * 100);
    }

    /**
     * Get rack utilization percentage.
     */
    public int getRackUtilizationPercent() {
        if (rackCapacityUnits == 0) return 0;
        return (int) ((double) totalRackUnitsUsed / rackCapacityUnits * 100);
    }
}

