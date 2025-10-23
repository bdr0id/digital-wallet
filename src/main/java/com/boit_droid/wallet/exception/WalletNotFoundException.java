package com.boit_droid.wallet.exception;

/**
 * Exception thrown when a wallet is not found
 */
public class WalletNotFoundException extends WalletException {
    
    private final String walletId;
    
    public WalletNotFoundException(String walletId) {
        super(String.format("Wallet not found with ID: %s", walletId), "WALLET_NOT_FOUND");
        this.walletId = walletId;
    }
    
    public String getWalletId() {
        return walletId;
    }
}