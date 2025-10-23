package com.boit_droid.wallet.exception;

/**
 * Exception thrown when a user is not found
 */
public class UserNotFoundException extends WalletException {
    
    private final String userId;
    
    public UserNotFoundException(String userId) {
        super(String.format("User not found with ID: %s", userId), "USER_NOT_FOUND");
        this.userId = userId;
    }
    
    public String getUserId() {
        return userId;
    }
}