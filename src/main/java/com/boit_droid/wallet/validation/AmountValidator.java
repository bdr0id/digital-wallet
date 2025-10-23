package com.boit_droid.wallet.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Validator for transaction amounts
 */
public class AmountValidator implements ConstraintValidator<ValidAmount, Double> {
    
    private double min;
    private double max;
    private int scale;
    
    @Override
    public void initialize(ValidAmount constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
        this.scale = constraintAnnotation.scale();
    }
    
    @Override
    public boolean isValid(Double amount, ConstraintValidatorContext context) {
        if (amount == null) {
            return false;
        }
        
        // Check if amount is within range
        if (amount < min || amount > max) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                String.format("Amount must be between %.2f and %.2f", min, max))
                   .addConstraintViolation();
            return false;
        }
        
        // Check decimal places
        BigDecimal bd = BigDecimal.valueOf(amount);
        if (bd.scale() > scale) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                String.format("Amount must have at most %d decimal places", scale))
                   .addConstraintViolation();
            return false;
        }
        
        // Check for negative zero and other edge cases
        if (amount.equals(-0.0) || amount.isNaN() || amount.isInfinite()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Invalid amount value")
                   .addConstraintViolation();
            return false;
        }
        
        return true;
    }
}