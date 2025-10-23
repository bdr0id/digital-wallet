package com.boit_droid.wallet.service.impl;

import com.boit_droid.wallet.entity.Transaction;
import com.boit_droid.wallet.entity.Wallet;
import com.boit_droid.wallet.service.TransactionSignatureService;
import com.boit_droid.wallet.util.SignatureUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Implementation of transaction signature verification service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionSignatureServiceImpl implements TransactionSignatureService {

    private final SignatureUtil signatureUtil;
    
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Override
    public String generateTransactionSignature(Wallet wallet, Double amount, String recipientWalletId, String transactionType) {
        try {
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
            String signatureData = signatureUtil.generateTransactionSignatureData(
                wallet.getWalletId(),
                wallet.getUser().getUserId(),
                amount.toString(),
                timestamp
            );
            
            // Include recipient wallet ID for transfer transactions
            if (recipientWalletId != null && !recipientWalletId.isEmpty()) {
                signatureData += "|" + recipientWalletId;
            }
            
            // Include transaction type
            signatureData += "|" + transactionType;
            
            String signature = signatureUtil.generateSignature(signatureData, wallet.getSalt());
            
            log.debug("Generated transaction signature for wallet: {} - Type: {}", 
                     wallet.getWalletId(), transactionType);
            
            return signature;
        } catch (Exception e) {
            log.error("Failed to generate transaction signature for wallet: {}", wallet.getWalletId(), e);
            throw new RuntimeException("Failed to generate transaction signature", e);
        }
    }

    @Override
    public boolean verifyTransactionSignature(Transaction transaction, String expectedSignature) {
        try {
            if (transaction == null || expectedSignature == null) {
                log.warn("Transaction or expected signature is null");
                return false;
            }
            
            Wallet senderWallet = transaction.getSenderWallet();
            if (senderWallet == null) {
                log.warn("Sender wallet is null for transaction: {}", transaction.getTransactionId());
                return false;
            }
            
            String timestamp = String.valueOf(transaction.getCreatedAt());
            String signatureData = signatureUtil.generateTransactionSignatureData(
                senderWallet.getWalletId(),
                senderWallet.getUser().getUserId(),
                transaction.getAmount().toString(),
                timestamp
            );
            
            // Include recipient wallet ID for transfer transactions
            if (transaction.getReceiverWallet() != null) {
                signatureData += "|" + transaction.getReceiverWallet().getWalletId();
            }
            
            // Include transaction type
            signatureData += "|" + transaction.getType().name();
            
            boolean isValid = signatureUtil.verifySignature(signatureData, expectedSignature, senderWallet.getSalt());
            
            if (!isValid) {
                log.warn("Transaction signature verification failed for transaction: {}", 
                        transaction.getTransactionId());
            }
            
            return isValid;
        } catch (Exception e) {
            log.error("Error verifying transaction signature for transaction: {}", 
                     transaction.getTransactionId(), e);
            return false;
        }
    }

    @Override
    public String generateWalletOperationSignature(Wallet wallet, String operation, String additionalData) {
        try {
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
            String signatureData = String.join("|",
                wallet.getWalletId(),
                wallet.getUser().getUserId(),
                operation,
                additionalData != null ? additionalData : "",
                timestamp
            );
            
            String signature = signatureUtil.generateSignature(signatureData, wallet.getSalt());
            
            log.debug("Generated wallet operation signature for wallet: {} - Operation: {}", 
                     wallet.getWalletId(), operation);
            
            return signature;
        } catch (Exception e) {
            log.error("Failed to generate wallet operation signature for wallet: {} - Operation: {}", 
                     wallet.getWalletId(), operation, e);
            throw new RuntimeException("Failed to generate wallet operation signature", e);
        }
    }

    @Override
    public boolean verifyWalletOperationSignature(Wallet wallet, String operation, String additionalData, String providedSignature) {
        try {
            if (wallet == null || operation == null || providedSignature == null) {
                log.warn("Wallet, operation, or provided signature is null");
                return false;
            }
            
            // For verification, we need to use a recent timestamp window
            // In a real implementation, you might store the timestamp with the signature
            LocalDateTime now = LocalDateTime.now();
            
            // Check signatures within a 5-minute window
            for (int i = 0; i < 5; i++) {
                String timestamp = now.minusMinutes(i).format(TIMESTAMP_FORMATTER);
                String signatureData = String.join("|",
                    wallet.getWalletId(),
                    wallet.getUser().getUserId(),
                    operation,
                    additionalData != null ? additionalData : "",
                    timestamp
                );
                
                if (signatureUtil.verifySignature(signatureData, providedSignature, wallet.getSalt())) {
                    log.debug("Wallet operation signature verified for wallet: {} - Operation: {}", 
                             wallet.getWalletId(), operation);
                    return true;
                }
            }
            
            log.warn("Wallet operation signature verification failed for wallet: {} - Operation: {}", 
                    wallet.getWalletId(), operation);
            return false;
        } catch (Exception e) {
            log.error("Error verifying wallet operation signature for wallet: {} - Operation: {}", 
                     wallet.getWalletId(), operation, e);
            return false;
        }
    }

    @Override
    public String generatePinVerificationSignature(String walletId, String hashedPin, String timestamp) {
        try {
            String signatureData = String.join("|", walletId, hashedPin, timestamp);
            
            // For PIN verification, we use a system-wide salt (could be from configuration)
            String systemSalt = "WALLET_PIN_VERIFICATION_SALT_2024";
            String signature = signatureUtil.generateSignature(signatureData, systemSalt);
            
            log.debug("Generated PIN verification signature for wallet: {}", walletId);
            
            return signature;
        } catch (Exception e) {
            log.error("Failed to generate PIN verification signature for wallet: {}", walletId, e);
            throw new RuntimeException("Failed to generate PIN verification signature", e);
        }
    }

    @Override
    public boolean verifyPinOperationSignature(String walletId, String hashedPin, String timestamp, String providedSignature) {
        try {
            if (walletId == null || hashedPin == null || timestamp == null || providedSignature == null) {
                log.warn("One or more required parameters for PIN signature verification is null");
                return false;
            }
            
            String signatureData = String.join("|", walletId, hashedPin, timestamp);
            String systemSalt = "WALLET_PIN_VERIFICATION_SALT_2024";
            
            boolean isValid = signatureUtil.verifySignature(signatureData, providedSignature, systemSalt);
            
            if (!isValid) {
                log.warn("PIN operation signature verification failed for wallet: {}", walletId);
            } else {
                log.debug("PIN operation signature verified for wallet: {}", walletId);
            }
            
            return isValid;
        } catch (Exception e) {
            log.error("Error verifying PIN operation signature for wallet: {}", walletId, e);
            return false;
        }
    }
}