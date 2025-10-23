package com.boit_droid.wallet.exception;

/**
 * Exception thrown when wallet status prevents an operation
 */
public class WalletStatusException extends WalletException {
    
    private final String currentStatus;
    private final String operation;
    
    public WalletStatusException(String operation, String currentStatus) {
        super(String.format("Cannot perform %s operation. Wallet status: %s", 
              operation, currentStatus), "WALLET_STATUS_ERROR");
        this.operation = operation;
        this.currentStatus = currentStatus;
    }
    
    public String getCurrentStatus() {
        return currentStatus;
    }
    
    public String getOperation() {
        return operation;
    }
}