package com.boit_droid.wallet.validation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom validation annotation for PIN security and complexity.
 * Validates that PINs are 4-6 digits long and are not weak patterns.
 * Weak patterns include: sequential numbers (1234), repeated digits (1111), etc.
 * 
 * @see PINValidator for validation logic
 */
@Documented
@Constraint(validatedBy = PINValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Schema(description = "Validates PIN security: 4-6 digits, no weak patterns like 1111 or 1234")
public @interface ValidPIN {
    /**
     * Error message when validation fails
     */
    String message() default "PIN must be 4-6 digits and cannot be weak (e.g., 1111, 1234)";
    
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}