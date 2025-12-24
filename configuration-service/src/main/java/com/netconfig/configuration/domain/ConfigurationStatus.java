package com.netconfig.configuration.domain;

/**
 * Status of a rack configuration.
 */
public enum ConfigurationStatus {
    DRAFT,      // Being edited
    VALIDATED,  // Passed all validation rules
    PRICED,     // Price has been calculated
    QUOTED,     // Quote has been generated
    ORDERED,    // Order placed
    ARCHIVED    // No longer active
}

