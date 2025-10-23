package com.boit_droid.wallet.validation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom validation annotation for currency codes.
 * Validates that the currency code is one of the supported currencies in the wallet system.
 * Supported currencies: USD, KES, EUR, GBP
 * 
 * @see CurrencyValidator for validation logic
 */
@Documented
@Constraint(validatedBy = CurrencyValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Schema(description = "Validates currency codes against supported currencies: USD, KES, EUR, GBP")
public @interface ValidCurrency {
    /**
     * Error message when validation fails
     */
    String message() default "Invalid currency code. Supported currencies: USD, KES, EUR, GBP";
    
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}