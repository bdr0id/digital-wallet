package com.boit_droid.wallet.service;

import com.boit_droid.wallet.entity.Transaction;
import com.boit_droid.wallet.entity.Wallet;

/**
 * Service interface for transaction signature verification and security
 */
public interface TransactionSignatureService {
    
    /**
     * Generate transaction signature for verification
     * @param wallet The wallet involved in the transaction
     * @param amount The transaction amount
     * @param recipientWalletId The recipient wallet ID (null for non-transfer transactions)
     * @param transactionType The type of transaction
     * @return Generated signature string
     */
    String generateTransactionSignature(Wallet wallet, Double amount, String recipientWalletId, String transactionType);
    
    /**
     * Verify transaction signature
     * @param transaction The transaction to verify
     * @param expectedSignature The expected signature
     * @return true if signature is valid, false otherwise
     */
    boolean verifyTransactionSignature(Transaction transaction, String expectedSignature);
    
    /**
     * Generate wallet operation signature for sensitive operations
     * @param wallet The wallet
     * @param operation The operation being performed
     * @param additionalData Additional data for signature generation
     * @return Generated signature string
     */
    String generateWalletOperationSignature(Wallet wallet, String operation, String additionalData);
    
    /**
     * Verify wallet operation signature
     * @param wallet The wallet
     * @param operation The operation
     * @param additionalData Additional data used in signature
     * @param providedSignature The signature to verify
     * @return true if signature is valid, false otherwise
     */
    boolean verifyWalletOperationSignature(Wallet wallet, String operation, String additionalData, String providedSignature);
    
    /**
     * Generate PIN verification signature
     * @param walletId The wallet ID
     * @param hashedPin The hashed PIN
     * @param timestamp The timestamp for signature
     * @return Generated signature string
     */
    String generatePinVerificationSignature(String walletId, String hashedPin, String timestamp);
    
    /**
     * Verify PIN operation signature
     * @param walletId The wallet ID
     * @param hashedPin The hashed PIN
     * @param timestamp The timestamp
     * @param providedSignature The signature to verify
     * @return true if signature is valid, false otherwise
     */
    boolean verifyPinOperationSignature(String walletId, String hashedPin, String timestamp, String providedSignature);
}