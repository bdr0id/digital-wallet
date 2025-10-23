package com.boit_droid.wallet.exception;

/**
 * Exception thrown when wallet balance is insufficient for a transaction
 */
public class InsufficientFundsException extends WalletException {
    
    private final double availableBalance;
    private final double requestedAmount;
    
    public InsufficientFundsException(double availableBalance, double requestedAmount) {
        super(String.format("Insufficient funds. Available: %.2f, Requested: %.2f", 
              availableBalance, requestedAmount), "INSUFFICIENT_FUNDS");
        this.availableBalance = availableBalance;
        this.requestedAmount = requestedAmount;
    }
    
    public double getAvailableBalance() {
        return availableBalance;
    }
    
    public double getRequestedAmount() {
        return requestedAmount;
    }
}