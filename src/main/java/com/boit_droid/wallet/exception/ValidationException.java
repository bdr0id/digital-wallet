package com.boit_droid.wallet.exception;

import java.util.List;

/**
 * Exception thrown when validation fails
 */
public class ValidationException extends WalletException {
    
    private final List<String> validationErrors;
    
    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR");
        this.validationErrors = List.of(message);
    }
    
    public ValidationException(List<String> validationErrors) {
        super("Validation failed", "VALIDATION_ERROR");
        this.validationErrors = validationErrors;
    }
    
    public List<String> getValidationErrors() {
        return validationErrors;
    }
}