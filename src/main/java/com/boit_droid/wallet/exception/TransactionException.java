package com.boit_droid.wallet.exception;

/**
 * Exception thrown when transaction processing fails
 */
public class TransactionException extends WalletException {
    
    private final String transactionId;
    
    public TransactionException(String message) {
        super(message, "TRANSACTION_FAILED");
        this.transactionId = null;
    }
    
    public TransactionException(String message, String transactionId) {
        super(String.format("Transaction failed [%s]: %s", transactionId, message), 
              "TRANSACTION_FAILED");
        this.transactionId = transactionId;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
}