package com.netconfig.common.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result of configuration validation.
 * Contains validation status and any error/warning messages.
 */
public record ValidationResult(
    boolean isValid,
    List<ValidationMessage> messages
) {
    public static ValidationResult success() {
        return new ValidationResult(true, Collections.emptyList());
    }

    public static ValidationResult failure(List<ValidationMessage> messages) {
        return new ValidationResult(false, messages);
    }

    public static ValidationResult failure(String error) {
        return new ValidationResult(false, List.of(
            new ValidationMessage(ValidationMessage.Severity.ERROR, error)
        ));
    }

    public ValidationResult merge(ValidationResult other) {
        if (other.isValid() && this.isValid()) {
            return success();
        }
        List<ValidationMessage> combined = new ArrayList<>(this.messages());
        combined.addAll(other.messages());
        return new ValidationResult(this.isValid() && other.isValid(), combined);
    }

    public record ValidationMessage(Severity severity, String message) {
        public enum Severity {
            ERROR,
            WARNING,
            INFO
        }
    }
}

