package com.boit_droid.wallet.service;

import com.boit_droid.wallet.entity.User;
import com.boit_droid.wallet.entity.Wallet;
import com.boit_droid.wallet.entity.Transaction;
import com.boit_droid.wallet.entity.Notification;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for comprehensive entity validation using Bean Validation annotations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EntityValidationService {

    private final Validator validator;

    /**
     * Validates a User entity and throws exception if validation fails
     */
    public void validateUser(User user) {
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException("User validation failed: " + errorMessage);
        }
    }

    /**
     * Validates a Wallet entity and throws exception if validation fails
     */
    public void validateWallet(Wallet wallet) {
        Set<ConstraintViolation<Wallet>> violations = validator.validate(wallet);
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException("Wallet validation failed: " + errorMessage);
        }
    }

    /**
     * Validates a Transaction entity and throws exception if validation fails
     */
    public void validateTransaction(Transaction transaction) {
        Set<ConstraintViolation<Transaction>> violations = validator.validate(transaction);
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException("Transaction validation failed: " + errorMessage);
        }
    }

    /**
     * Validates a Notification entity and throws exception if validation fails
     */
    public void validateNotification(Notification notification) {
        Set<ConstraintViolation<Notification>> violations = validator.validate(notification);
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            throw new IllegalArgumentException("Notification validation failed: " + errorMessage);
        }
    }

    /**
     * Validates any entity and returns validation results without throwing exception
     */
    public <T> Set<ConstraintViolation<T>> validateEntity(T entity) {
        return validator.validate(entity);
    }

    /**
     * Checks if an entity is valid
     */
    public <T> boolean isValid(T entity) {
        return validator.validate(entity).isEmpty();
    }

    /**
     * Gets validation error messages for an entity
     */
    public <T> String getValidationErrors(T entity) {
        Set<ConstraintViolation<T>> violations = validator.validate(entity);
        return violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
    }
}