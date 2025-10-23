package com.boit_droid.wallet.exception;

/**
 * Exception thrown when KYC verification fails
 */
public class KYCVerificationException extends WalletException {
    
    private final String reason;
    
    public KYCVerificationException(String reason) {
        super(String.format("KYC verification failed: %s", reason), "KYC_VERIFICATION_FAILED");
        this.reason = reason;
    }
    
    public String getReason() {
        return reason;
    }
}