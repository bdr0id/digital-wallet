package com.boit_droid.wallet.validation;

import com.boit_droid.wallet.util.SignatureUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Validator for PIN security and complexity
 */
@Component
@RequiredArgsConstructor
public class PINValidator implements ConstraintValidator<ValidPIN, String> {
    
    private final SignatureUtil signatureUtil;
    
    @Override
    public void initialize(ValidPIN constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(String pin, ConstraintValidatorContext context) {
        if (pin == null || pin.trim().isEmpty()) {
            return false;
        }
        
        try {
            // Use SignatureUtil to validate PIN
            signatureUtil.validatePin(pin);
            return true;
        } catch (IllegalArgumentException e) {
            // Update the validation message with specific error
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(e.getMessage())
                   .addConstraintViolation();
            return false;
        }
    }
}