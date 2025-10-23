package com.boit_droid.wallet.service;

import com.boit_droid.wallet.dto.request.StatementRequest;
import com.boit_droid.wallet.dto.response.CustomApiResponse;
import com.boit_droid.wallet.dto.response.PagedResponse;
import com.boit_droid.wallet.dto.response.TransactionResponse;
import com.boit_droid.wallet.entity.enums.Status;
import com.boit_droid.wallet.entity.enums.TransactionType;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Service interface for transaction and statement operations
 * Handles transaction history retrieval, statement generation, and audit trail functionality
 */
public interface TransactionService {
    
    /**
     * Generate account statement for a wallet with date filtering
     * Requirements: 6.1, 6.2, 6.3, 6.4, 6.5
     */
    CustomApiResponse generateAccountStatement(String walletId, StatementRequest request);
    
    /**
     * Retrieve transaction history for a wallet with pagination
     * Requirements: 6.1, 6.2, 6.3
     */
    PagedResponse<TransactionResponse> getTransactionHistory(String walletId, Pageable pageable);
    
    /**
     * Retrieve transaction history for a wallet within date range with pagination
     * Requirements: 6.1, 6.2, 6.3
     */
    PagedResponse<TransactionResponse> getTransactionHistoryByDateRange(
            String walletId, 
            Instant startDate,
            Instant endDate,
            Pageable pageable);
    
    /**
     * Get transaction details by transaction ID
     * Requirements: 6.1, 6.2
     */
    CustomApiResponse getTransactionDetails(String transactionId);
    
    /**
     * Search transactions with multiple filters
     * Requirements: 6.1, 6.2, 6.3
     */
    PagedResponse<TransactionResponse> searchTransactions(
            String walletId,
            TransactionType type,
            Status status,
            String currency,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Instant startDate,
            Instant endDate,
            String channel,
            Pageable pageable);
    
    /**
     * Get transaction audit trail for compliance and tracking
     * Requirements: 10.1, 10.2, 10.3, 10.4, 10.5
     */
    CustomApiResponse getTransactionAuditTrail(String transactionId);
    
    /**
     * Get comprehensive audit trail for a wallet
     * Requirements: 10.1, 10.2, 10.3, 10.4, 10.5
     */
    PagedResponse<TransactionResponse> getWalletAuditTrail(String walletId, Pageable pageable);
    
    /**
     * Get transaction statistics for a wallet
     * Requirements: 6.1, 6.2, 6.3
     */
    CustomApiResponse getTransactionStatistics(String walletId, Instant startDate, Instant endDate);
    
    /**
     * Verify transaction integrity and consistency
     * Requirements: 10.1, 10.2, 10.3, 10.4, 10.5
     */
    CustomApiResponse verifyTransactionIntegrity(String transactionId);
    
    /**
     * Get pending transactions for a wallet
     * Requirements: 6.1, 6.2
     */
    List<TransactionResponse> getPendingTransactions(String walletId);
    
    /**
     * Get failed transactions for a wallet
     * Requirements: 6.1, 6.2, 10.1, 10.2
     */
    List<TransactionResponse> getFailedTransactions(String walletId);
    
    /**
     * Calculate transaction volume for a wallet within date range
     * Requirements: 6.1, 6.2, 6.3
     */
    CustomApiResponse calculateTransactionVolume(String walletId, Instant startDate, Instant endDate);
}