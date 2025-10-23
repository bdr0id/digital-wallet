package com.boit_droid.wallet.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

/**
 * Validator for currency codes
 */
public class CurrencyValidator implements ConstraintValidator<ValidCurrency, String> {
    
    private static final Set<String> SUPPORTED_CURRENCIES = Set.of("USD", "KES", "EUR", "GBP");
    
    @Override
    public void initialize(ValidCurrency constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(String currency, ConstraintValidatorContext context) {
        if (currency == null) {
            return false;
        }
        
        return SUPPORTED_CURRENCIES.contains(currency.toUpperCase());
    }
}