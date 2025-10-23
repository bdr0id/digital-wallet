package com.boit_droid.wallet.validation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom validation annotation for transaction amounts.
 * Validates that amounts are positive, within specified range, and have appropriate decimal precision.
 * 
 * @see AmountValidator for validation logic
 */
@Documented
@Constraint(validatedBy = AmountValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Schema(description = "Validates transaction amounts with configurable min/max values and decimal precision")
public @interface ValidAmount {
    /**
     * Error message when validation fails
     */
    String message() default "Amount must be positive and within allowed limits";
    
    /**
     * Minimum allowed amount (inclusive)
     */
    double min() default 0.01;
    
    /**
     * Maximum allowed amount (inclusive)
     */
    double max() default 1000000.0;
    
    /**
     * Maximum number of decimal places allowed
     */
    int scale() default 2;
    
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}