package com.netconfig.common.exception;

import com.netconfig.common.dto.ValidationResult;

/**
 * Exception thrown when configuration validation fails.
 */
public class ValidationException extends RuntimeException {
    
    private final ValidationResult validationResult;

    public ValidationException(ValidationResult validationResult) {
        super("Configuration validation failed");
        this.validationResult = validationResult;
    }

    public ValidationException(String message) {
        super(message);
        this.validationResult = ValidationResult.failure(message);
    }

    public ValidationResult getValidationResult() {
        return validationResult;
    }
}

