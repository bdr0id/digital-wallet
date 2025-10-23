package com.boit_droid.wallet.exception;

/**
 * Exception thrown when PIN validation fails
 */
public class InvalidPINException extends WalletException {
    
    private final int attemptsRemaining;
    
    public InvalidPINException() {
        super("Invalid PIN provided", "INVALID_PIN");
        this.attemptsRemaining = -1;
    }
    
    public InvalidPINException(int attemptsRemaining) {
        super(String.format("Invalid PIN provided. Attempts remaining: %d", attemptsRemaining), 
              "INVALID_PIN");
        this.attemptsRemaining = attemptsRemaining;
    }
    
    public int getAttemptsRemaining() {
        return attemptsRemaining;
    }
}