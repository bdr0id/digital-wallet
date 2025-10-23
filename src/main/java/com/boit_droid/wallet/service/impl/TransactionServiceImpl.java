package com.boit_droid.wallet.service.impl;

import com.boit_droid.wallet.dto.request.StatementRequest;
import com.boit_droid.wallet.dto.response.CustomApiResponse;
import com.boit_droid.wallet.dto.response.PagedResponse;
import com.boit_droid.wallet.dto.response.StatementResponse;
import com.boit_droid.wallet.dto.response.TransactionResponse;
import com.boit_droid.wallet.entity.Transaction;
import com.boit_droid.wallet.entity.Wallet;
import com.boit_droid.wallet.entity.enums.Status;
import com.boit_droid.wallet.entity.enums.TransactionType;
import com.boit_droid.wallet.repository.TransactionRepository;
import com.boit_droid.wallet.repository.WalletRepository;
import com.boit_droid.wallet.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of TransactionService for handling transaction and statement operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;

    @Override
    public CustomApiResponse generateAccountStatement(String walletId, StatementRequest request) {
        log.info("Generating account statement for wallet: {} from {} to {}", 
                walletId, request.getStartDate(), request.getEndDate());
        
        try {
            // Find wallet (accepts either requestId or accountNumber)
            Optional<Wallet> walletOpt = resolveWalletByIdOrAccount(walletId);
            if (walletOpt.isEmpty()) {
                return CustomApiResponse.error("Wallet not found", "WALLET_NOT_FOUND",List.of("Wallet not found"));
            }
            
            Wallet wallet = walletOpt.get();
            
            // Convert dates to Instant for database queries (normalize to UTC)
            Instant startDateTime = request.getStartDate()
                    .atStartOfDay(ZoneOffset.UTC)
                    .toInstant();
            Instant endDateTime = request.getEndDate()
                    .atTime(LocalTime.MAX)
                    .atOffset(ZoneOffset.UTC)
                    .toInstant();
            
            // Get transactions within date range
            Page<Transaction> transactionPage = transactionRepository.findByWalletAndDateRange(
                    wallet, startDateTime, endDateTime, 
                    Pageable.ofSize(request.getSize()).withPage(request.getPage()));
            
            // Calculate balances and totals
            BigDecimal openingBalance = calculateOpeningBalance(wallet, startDateTime);
            BigDecimal totalCredits = BigDecimal.ZERO;
            BigDecimal totalDebits = BigDecimal.ZERO;
            
            List<TransactionResponse> transactionResponses = new ArrayList<>();
            
            for (Transaction transaction : transactionPage.getContent()) {
                TransactionResponse response = mapToTransactionResponse(transaction);
                transactionResponses.add(response);
                
                // Calculate credits and debits
                if (transaction.getReceiverWallet().equals(wallet)) {
                    totalCredits = totalCredits.add(transaction.getAmount());
                }
                if (transaction.getSenderWallet() != null && transaction.getSenderWallet().equals(wallet)) {
                    totalDebits = totalDebits.add(transaction.getAmount());
                }
            }
            
            BigDecimal closingBalance = openingBalance.add(totalCredits).subtract(totalDebits);
            
            // Build statement response
            StatementResponse statement = new StatementResponse();
            statement.setWalletId(wallet.getAccountNumber());
            statement.setAccountNumber(wallet.getAccountNumber());
            statement.setAccountName(wallet.getAccountName());
            statement.setStartDate(request.getStartDate());
            statement.setEndDate(request.getEndDate());
            statement.setOpeningBalance(openingBalance.doubleValue());
            statement.setClosingBalance(closingBalance.doubleValue());
            statement.setTotalCredits(totalCredits.doubleValue());
            statement.setTotalDebits(totalDebits.doubleValue());
            statement.setTransactionCount(transactionResponses.size());
            statement.setTransactions(transactionResponses);
            statement.setGeneratedAt(Instant.now());
            
            log.info("Successfully generated statement for wallet: {} with {} transactions", 
                    walletId, transactionResponses.size());
            
            return CustomApiResponse.success("Account statement generated successfully", statement);
            
        } catch (Exception e) {
            log.error("Error generating account statement for wallet: {}", walletId, e);
            return CustomApiResponse.error("Failed to generate account statement: " + e.getMessage(),
                    "STATEMENT_GENERATION_ERROR",List.of("Failed to generate account statement"));
        }
    }

    @Override
    public PagedResponse<TransactionResponse> getTransactionHistory(String walletId, Pageable pageable) {
        log.info("Retrieving transaction history for wallet: {}", walletId);
        
        try {
            Optional<Wallet> walletOpt = resolveWalletByIdOrAccount(walletId);
            if (walletOpt.isEmpty()) {
                return PagedResponse.empty("Wallet not found");
            }
            
            Wallet wallet = walletOpt.get();
            Page<Transaction> transactionPage = transactionRepository
                    .findBySenderWalletOrReceiverWalletOrderByCreatedAtDesc(wallet, wallet, pageable);
            
            List<TransactionResponse> responses = transactionPage.getContent().stream()
                    .map(this::mapToTransactionResponse)
                    .collect(Collectors.toList());
            
            return PagedResponse.of(responses, transactionPage);
            
        } catch (Exception e) {
            log.error("Error retrieving transaction history for wallet: {}", walletId, e);
            return PagedResponse.empty("Failed to retrieve transaction history");
        }
    }

    @Override
    public PagedResponse<TransactionResponse> getTransactionHistoryByDateRange(
            String walletId, Instant startDate, Instant endDate, Pageable pageable) {
        
        log.info("Retrieving transaction history for wallet: {} from {} to {}", 
                walletId, startDate, endDate);
        
        try {
            Optional<Wallet> walletOpt = resolveWalletByIdOrAccount(walletId);
            if (walletOpt.isEmpty()) {
                return PagedResponse.empty("Wallet not found");
            }
            
            Wallet wallet = walletOpt.get();
            Page<Transaction> transactionPage = transactionRepository
                    .findByWalletAndDateRange(wallet, startDate, endDate, pageable);
            
            List<TransactionResponse> responses = transactionPage.getContent().stream()
                    .map(this::mapToTransactionResponse)
                    .collect(Collectors.toList());
            
            return PagedResponse.of(responses, transactionPage);
            
        } catch (Exception e) {
            log.error("Error retrieving transaction history by date range for wallet: {}", walletId, e);
            return PagedResponse.empty("Failed to retrieve transaction history");
        }
    }

    @Override
    public CustomApiResponse getTransactionDetails(String transactionId) {
        log.info("Retrieving transaction details for: {}", transactionId);
        
        try {
            Optional<Transaction> transactionOpt = transactionRepository.findByRequestId(transactionId);
            if (transactionOpt.isEmpty()) {
                return CustomApiResponse.error("Transaction not found", "TRANSACTION_NOT_FOUND",List.of("Transaction not found"));
            }
            
            Transaction transaction = transactionOpt.get();
            TransactionResponse response = mapToTransactionResponse(transaction);
            
            // Add additional audit details
            Map<String, Object> auditDetails = new HashMap<>();
            auditDetails.put("transaction", response);
            auditDetails.put("auditTrail", buildAuditTrail(transaction));
            
            return CustomApiResponse.success("Transaction details retrieved successfully", auditDetails);
            
        } catch (Exception e) {
            log.error("Error retrieving transaction details for: {}", transactionId, e);
            return CustomApiResponse.error("Failed to retrieve transaction details: " + e.getMessage(),
                    "TRANSACTION_DETAILS_ERROR", List.of("Failed to retrieve transaction details"));
        }
    } 
   @Override
    public PagedResponse<TransactionResponse> searchTransactions(
            String walletId, TransactionType type, Status status, String currency,
            BigDecimal minAmount, BigDecimal maxAmount, Instant startDate,
            Instant endDate, String channel, Pageable pageable) {
        
        log.info("Searching transactions for wallet: {} with filters", walletId);
        
        try {
            Long walletDbId = null;
            if (walletId != null) {
                Optional<Wallet> walletOpt = walletRepository.findByAccountNumber(walletId);
                if (walletOpt.isEmpty()) {
                    return PagedResponse.empty("Wallet not found");
                }
                walletDbId = walletOpt.get().getId();
            }
            
            Page<Transaction> transactionPage = transactionRepository.searchTransactions(
                    walletDbId, type, status, currency, minAmount, maxAmount, 
                    startDate, endDate, channel, pageable);
            
            List<TransactionResponse> responses = transactionPage.getContent().stream()
                    .map(this::mapToTransactionResponse)
                    .collect(Collectors.toList());
            
            return PagedResponse.of(responses, transactionPage);
            
        } catch (Exception e) {
            log.error("Error searching transactions for wallet: {}", walletId, e);
            return PagedResponse.empty("Failed to search transactions");
        }
    }

    @Override
    public CustomApiResponse getTransactionAuditTrail(String transactionId) {
        log.info("Retrieving audit trail for transaction: {}", transactionId);
        
        try {
            Optional<Transaction> transactionOpt = transactionRepository.findByRequestId(transactionId);
            if (transactionOpt.isEmpty()) {
                return CustomApiResponse.error("Transaction not found", "TRANSACTION_NOT_FOUND", List.of("Transaction not found"));
            }
            
            Transaction transaction = transactionOpt.get();
            Map<String, Object> auditTrail = buildComprehensiveAuditTrail(transaction);
            
            return CustomApiResponse.success("Transaction audit trail retrieved successfully", auditTrail);
            
        } catch (Exception e) {
            log.error("Error retrieving audit trail for transaction: {}", transactionId, e);
            return CustomApiResponse.error("Failed to retrieve audit trail: " + e.getMessage(),
                    "AUDIT_TRAIL_ERROR", List.of("Failed to retrieve audit trail"));
        }
    }

    @Override
    public PagedResponse<TransactionResponse> getWalletAuditTrail(String walletId, Pageable pageable) {
        log.info("Retrieving comprehensive audit trail for wallet: {}", walletId);
        
        try {
            Optional<Wallet> walletOpt = resolveWalletByIdOrAccount(walletId);
            if (walletOpt.isEmpty()) {
                return PagedResponse.empty("Wallet not found");
            }
            
            Wallet wallet = walletOpt.get();
            
            // Get all transactions including soft-deleted ones for complete audit trail
            Page<Transaction> transactionPage = transactionRepository
                    .findBySenderWalletOrReceiverWalletOrderByCreatedAtDesc(wallet, wallet, pageable);
            
            List<TransactionResponse> responses = transactionPage.getContent().stream()
                    .map(transaction -> {
                        TransactionResponse response = mapToTransactionResponse(transaction);
                        // Add audit-specific fields
                        response.setReference(transaction.getExternalReference());
                        return response;
                    })
                    .collect(Collectors.toList());
            
            return PagedResponse.of(responses, transactionPage);
            
        } catch (Exception e) {
            log.error("Error retrieving wallet audit trail for: {}", walletId, e);
            return PagedResponse.empty("Failed to retrieve wallet audit trail");
        }
    }

    @Override
    public CustomApiResponse getTransactionStatistics(String walletId, Instant startDate, Instant endDate) {
        log.info("Calculating transaction statistics for wallet: {} from {} to {}", 
                walletId, startDate, endDate);
        
        try {
            Optional<Wallet> walletOpt = resolveWalletByIdOrAccount(walletId);
            if (walletOpt.isEmpty()) {
                return CustomApiResponse.error("Wallet not found", "WALLET_NOT_FOUND", List.of("Wallet not found"));
            }
            
            Wallet wallet = walletOpt.get();
            
            // Calculate various statistics
            Map<String, Object> statistics = new HashMap<>();
            
            // Transaction counts by type
            statistics.put("totalTransactions", transactionRepository.countByWallet(wallet));
            statistics.put("pendingTransactions", 
                    transactionRepository.findBySenderWalletOrReceiverWalletOrderByCreatedAtDesc(wallet, wallet)
                            .stream().filter(t -> t.getStatus() == Status.PENDING).count());
            statistics.put("completedTransactions", 
                    transactionRepository.findBySenderWalletOrReceiverWalletOrderByCreatedAtDesc(wallet, wallet)
                            .stream().filter(t -> t.getStatus() == Status.ACTIVE).count());
            statistics.put("failedTransactions", 
                    transactionRepository.findBySenderWalletOrReceiverWalletOrderByCreatedAtDesc(wallet, wallet)
                            .stream().filter(t -> t.getStatus() == Status.CANCELLED).count());
            
            // Volume calculations
            BigDecimal totalCredits = transactionRepository.sumAmountByReceiverWalletAndType(
                    wallet, TransactionType.DEPOSIT) != null ? 
                    transactionRepository.sumAmountByReceiverWalletAndType(wallet, TransactionType.DEPOSIT) : 
                    BigDecimal.ZERO;
            
            BigDecimal totalDebits = transactionRepository.sumAmountBySenderWalletAndType(
                    wallet, TransactionType.TRANSFER) != null ? 
                    transactionRepository.sumAmountBySenderWalletAndType(wallet, TransactionType.TRANSFER) : 
                    BigDecimal.ZERO;
            
            statistics.put("totalCredits", totalCredits);
            statistics.put("totalDebits", totalDebits);
            statistics.put("netFlow", totalCredits.subtract(totalDebits));
            
            // Daily and monthly volumes
            if (startDate != null && endDate != null) {
                statistics.put("periodVolume", calculatePeriodVolume(wallet, startDate, endDate));
            }
            
            return CustomApiResponse.success("Transaction statistics calculated successfully", statistics);
            
        } catch (Exception e) {
            log.error("Error calculating transaction statistics for wallet: {}", walletId, e);
            return CustomApiResponse.error("Failed to calculate transaction statistics: " + e.getMessage(),
                    "STATISTICS_ERROR", List.of("Failed to calculate transaction statistics:"));
        }
    }

    @Override
    public CustomApiResponse verifyTransactionIntegrity(String transactionId) {
        log.info("Verifying transaction integrity for: {}", transactionId);
        
        try {
            Optional<Transaction> transactionOpt = transactionRepository.findByRequestId(transactionId);
            if (transactionOpt.isEmpty()) {
                return CustomApiResponse.error("Transaction not found", "TRANSACTION_NOT_FOUND",List.of("Transaction not found"));
            }
            
            Transaction transaction = transactionOpt.get();
            Map<String, Object> verificationResult = new HashMap<>();
            
            // Verify balance consistency
            boolean balanceConsistent = verifyBalanceConsistency(transaction);
            verificationResult.put("balanceConsistent", balanceConsistent);
            
            // Verify transaction signature if present
            boolean signatureValid = verifyTransactionSignature(transaction);
            verificationResult.put("signatureValid", signatureValid);
            
            // Verify transaction hash if present
            boolean hashValid = verifyTransactionHash(transaction);
            verificationResult.put("hashValid", hashValid);
            
            // Check for duplicate transactions
            boolean isDuplicate = checkForDuplicates(transaction);
            verificationResult.put("isDuplicate", isDuplicate);
            
            // Overall integrity status
            boolean integrityValid = balanceConsistent && signatureValid && hashValid && !isDuplicate;
            verificationResult.put("integrityValid", integrityValid);
            
            // Add verification timestamp
            verificationResult.put("verifiedAt", Instant.now());
            verificationResult.put("verificationId", UUID.randomUUID().toString());
            
            return CustomApiResponse.success("Transaction integrity verification completed", verificationResult);
            
        } catch (Exception e) {
            log.error("Error verifying transaction integrity for: {}", transactionId, e);
            return CustomApiResponse.error("Failed to verify transaction integrity: " + e.getMessage(),
                    "INTEGRITY_VERIFICATION_ERROR", List.of("Failed to verify transaction integrity"));
        }
    }

    @Override
    public List<TransactionResponse> getPendingTransactions(String walletId) {
        log.info("Retrieving pending transactions for wallet: {}", walletId);
        
        try {
            Optional<Wallet> walletOpt = walletRepository.findByAccountNumber(walletId);
            if (walletOpt.isEmpty()) {
                return Collections.emptyList();
            }
            
            Wallet wallet = walletOpt.get();
            List<Transaction> pendingTransactions = transactionRepository
                    .findBySenderWalletAndStatusOrderByCreatedAtAsc(wallet, Status.PENDING);
            
            pendingTransactions.addAll(transactionRepository
                    .findByReceiverWalletAndStatusOrderByCreatedAtAsc(wallet, Status.PENDING));
            
            return pendingTransactions.stream()
                    .map(this::mapToTransactionResponse)
                    .collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("Error retrieving pending transactions for wallet: {}", walletId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<TransactionResponse> getFailedTransactions(String walletId) {
        log.info("Retrieving failed transactions for wallet: {}", walletId);
        
        try {
            Optional<Wallet> walletOpt = walletRepository.findByAccountNumber(walletId);
            if (walletOpt.isEmpty()) {
                return Collections.emptyList();
            }
            
            Wallet wallet = walletOpt.get();
            List<Transaction> failedTransactions = transactionRepository
                    .findBySenderWalletAndStatusOrderByCreatedAtAsc(wallet, Status.CANCELLED);
            
            failedTransactions.addAll(transactionRepository
                    .findByReceiverWalletAndStatusOrderByCreatedAtAsc(wallet, Status.CANCELLED));
            
            return failedTransactions.stream()
                    .map(this::mapToTransactionResponse)
                    .collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("Error retrieving failed transactions for wallet: {}", walletId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public CustomApiResponse calculateTransactionVolume(String walletId, Instant startDate, Instant endDate) {
        log.info("Calculating transaction volume for wallet: {} from {} to {}", 
                walletId, startDate, endDate);
        
        try {
            Optional<Wallet> walletOpt = walletRepository.findByAccountNumber(walletId);
            if (walletOpt.isEmpty()) {
                return CustomApiResponse.error("Wallet not found", "WALLET_NOT_FOUND",List.of("Wallet not found"));
            }
            
            Wallet wallet = walletOpt.get();
            Map<String, Object> volumeData = calculatePeriodVolume(wallet, startDate, endDate);
            
            return CustomApiResponse.success("Transaction volume calculated successfully", volumeData);
            
        } catch (Exception e) {
            log.error("Error calculating transaction volume for wallet: {}", walletId, e);
            return CustomApiResponse.error("Failed to calculate transaction volume: " + e.getMessage(),
                    "VOLUME_CALCULATION_ERROR", List.of("Failed to calculate transaction volume"));
        }
    }
    //Helper methods
    /**
     * Resolve a wallet by either requestId or accountNumber
     */
    private Optional<Wallet> resolveWalletByIdOrAccount(String walletIdOrAccount) {
        try {
            if (walletIdOrAccount == null || walletIdOrAccount.isBlank()) {
                return Optional.empty();
            }
            Optional<Wallet> byRequestId = walletRepository.findByRequestId(walletIdOrAccount);
            if (byRequestId.isPresent()) {
                return byRequestId;
            }
            return walletRepository.findByAccountNumber(walletIdOrAccount);
        } catch (Exception e) {
            log.warn("Error resolving wallet by id/account {}: {}", walletIdOrAccount, e.getMessage());
            return Optional.empty();
        }
    }
    
    private TransactionResponse mapToTransactionResponse(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setTransactionId(transaction.getRequestId());
        response.setSenderWalletId(transaction.getSenderWallet() != null ? 
                transaction.getSenderWallet().getAccountNumber() : null);
        response.setReceiverWalletId(transaction.getReceiverWallet().getAccountNumber());
        response.setSenderAccountNumber(transaction.getSenderWallet() != null ? 
                transaction.getSenderWallet().getAccountNumber() : null);
        response.setReceiverAccountNumber(transaction.getReceiverWallet().getAccountNumber());
        response.setAmount(transaction.getAmount().doubleValue());
        response.setCurrency(transaction.getCurrency());
        response.setType(transaction.getType().name());
        response.setStatus(transaction.getStatus().name());
        response.setDescription(transaction.getDescription());
        response.setReference(transaction.getExternalReference());
        response.setTimestamp(transaction.getCreatedAt());
        response.setCreatedAt(transaction.getCreatedAt());
        return response;
    }
    
    private BigDecimal calculateOpeningBalance(Wallet wallet, Instant startDate) {
        try {
            // Get all completed transactions before the start date
            List<Transaction> previousTransactions = transactionRepository
                    .findBySenderWalletOrReceiverWalletOrderByCreatedAtDesc(wallet, wallet)
                    .stream()
                    .filter(t -> t.getCreatedAt().isBefore(startDate) && t.getStatus() == Status.ACTIVE)
                    .collect(Collectors.toList());
            
            BigDecimal balance = BigDecimal.ZERO;
            
            for (Transaction transaction : previousTransactions) {
                if (transaction.getReceiverWallet().equals(wallet)) {
                    balance = balance.add(transaction.getAmount());
                }
                if (transaction.getSenderWallet() != null && transaction.getSenderWallet().equals(wallet)) {
                    balance = balance.subtract(transaction.getAmount());
                }
            }
            
            return balance;
        } catch (Exception e) {
            log.warn("Error calculating opening balance for wallet: {}, using current balance", 
                    wallet.getAccountNumber(), e);
            return wallet.getBalance();
        }
    }
    
    private Map<String, Object> buildAuditTrail(Transaction transaction) {
        Map<String, Object> auditTrail = new HashMap<>();
        auditTrail.put("transactionId", transaction.getRequestId());
        auditTrail.put("createdAt", transaction.getCreatedAt());
        auditTrail.put("createdBy", transaction.getCreatedBy());
        auditTrail.put("lastModifiedAt", transaction.getLastModifiedAt());
        auditTrail.put("lastModifiedBy", transaction.getLastModifiedBy());
        auditTrail.put("status", transaction.getStatus());
        auditTrail.put("processedAt", transaction.getProcessedAt());
        auditTrail.put("processedBy", transaction.getProcessedBy());
        auditTrail.put("verified", transaction.getVerified());
        auditTrail.put("verifiedAt", transaction.getVerifiedAt());
        auditTrail.put("verifiedBy", transaction.getVerifiedBy());
        return auditTrail;
    }
    
    private Map<String, Object> buildComprehensiveAuditTrail(Transaction transaction) {
        Map<String, Object> auditTrail = new HashMap<>();
        
        // Basic transaction info
        auditTrail.put("transactionId", transaction.getRequestId());
        auditTrail.put("amount", transaction.getAmount());
        auditTrail.put("currency", transaction.getCurrency());
        auditTrail.put("type", transaction.getType());
        auditTrail.put("status", transaction.getStatus());
        
        // Timestamps
        auditTrail.put("createdAt", transaction.getCreatedAt());
        auditTrail.put("processedAt", transaction.getProcessedAt());
        auditTrail.put("completedAt", transaction.getCompletedAt());
        auditTrail.put("lastModifiedAt", transaction.getLastModifiedAt());
        
        // User tracking
        auditTrail.put("createdBy", transaction.getCreatedBy());
        auditTrail.put("processedBy", transaction.getProcessedBy());
        auditTrail.put("lastModifiedBy", transaction.getLastModifiedBy());
        auditTrail.put("initiatedBy", transaction.getInitiatedBy());
        
        // Security info
        auditTrail.put("sourceIp", transaction.getSourceIp());
        auditTrail.put("userAgent", transaction.getUserAgent());
        auditTrail.put("deviceId", transaction.getDeviceId());
        auditTrail.put("channel", transaction.getChannel());
        
        // Balance tracking
        auditTrail.put("senderBalanceBefore", transaction.getSenderBalanceBefore());
        auditTrail.put("senderBalanceAfter", transaction.getSenderBalanceAfter());
        auditTrail.put("receiverBalanceBefore", transaction.getReceiverBalanceBefore());
        auditTrail.put("receiverBalanceAfter", transaction.getReceiverBalanceAfter());
        
        // Verification info
        auditTrail.put("verified", transaction.getVerified());
        auditTrail.put("verifiedAt", transaction.getVerifiedAt());
        auditTrail.put("verifiedBy", transaction.getVerifiedBy());
        auditTrail.put("transactionSignature", transaction.getTransactionSignature());
        auditTrail.put("transactionHash", transaction.getTransactionHash());
        
        // Error tracking
        auditTrail.put("errorMessage", transaction.getErrorMessage());
        auditTrail.put("errorCode", transaction.getErrorCode());
        auditTrail.put("retryCount", transaction.getRetryCount());
        auditTrail.put("lastRetryAt", transaction.getLastRetryAt());
        
        // External references
        auditTrail.put("externalReference", transaction.getExternalReference());
        auditTrail.put("batchId", transaction.getBatchId());
        auditTrail.put("correlationId", transaction.getCorrelationId());
        auditTrail.put("parentTransactionId", transaction.getParentTransactionId());
        
        // Compliance
        auditTrail.put("regulatoryCode", transaction.getRegulatoryCode());
        auditTrail.put("requiresApproval", transaction.getRequiresApproval());
        auditTrail.put("approvedBy", transaction.getApprovedBy());
        auditTrail.put("approvedAt", transaction.getApprovedAt());
        auditTrail.put("approvalComments", transaction.getApprovalComments());
        
        // Notification tracking
        auditTrail.put("senderNotified", transaction.getSenderNotified());
        auditTrail.put("receiverNotified", transaction.getReceiverNotified());
        auditTrail.put("senderNotifiedAt", transaction.getSenderNotifiedAt());
        auditTrail.put("receiverNotifiedAt", transaction.getReceiverNotifiedAt());
        
        return auditTrail;
    }
    
    private boolean verifyBalanceConsistency(Transaction transaction) {
        try {
            if (transaction.getSenderWallet() != null && 
                transaction.getSenderBalanceBefore() != null && 
                transaction.getSenderBalanceAfter() != null) {
                
                BigDecimal expectedAfterBalance = transaction.getSenderBalanceBefore()
                        .subtract(transaction.getAmount());
                
                return transaction.getSenderBalanceAfter().compareTo(expectedAfterBalance) == 0;
            }
            return true; // No sender wallet (deposit transaction)
        } catch (Exception e) {
            log.warn("Error verifying balance consistency for transaction: {}", 
                    transaction.getRequestId(), e);
            return false;
        }
    }
    
    private boolean verifyTransactionSignature(Transaction transaction) {
        try {
            if (transaction.getTransactionSignature() == null) {
                return true; // No signature to verify
            }
            
            // TODO: Implement actual signature verification logic
            // This would involve cryptographic verification using the wallet's public key
            log.info("Transaction signature verification not yet implemented for: {}", 
                    transaction.getRequestId());
            return true;
        } catch (Exception e) {
            log.warn("Error verifying transaction signature for: {}", transaction.getRequestId(), e);
            return false;
        }
    }
    
    private boolean verifyTransactionHash(Transaction transaction) {
        try {
            if (transaction.getTransactionHash() == null) {
                return true; // No hash to verify
            }
            
            // TODO: Implement actual hash verification logic
            // This would involve recalculating the hash and comparing
            log.info("Transaction hash verification not yet implemented for: {}", 
                    transaction.getRequestId());
            return true;
        } catch (Exception e) {
            log.warn("Error verifying transaction hash for: {}", transaction.getRequestId(), e);
            return false;
        }
    }
    
    private boolean checkForDuplicates(Transaction transaction) {
        try {
            if (transaction.getSenderWallet() == null) {
                return false; // Deposit transactions are less likely to be duplicates
            }

            Instant startTime = transaction.getCreatedAt().minusSeconds(300);
            Instant endTime = transaction.getCreatedAt().plusSeconds(300);
            
            List<Transaction> potentialDuplicates = transactionRepository.findPotentialDuplicates(
                    transaction.getSenderWallet(),
                    transaction.getReceiverWallet(),
                    transaction.getAmount(),
                    startTime,
                    endTime,
                    transaction.getId()
            );
            
            return !potentialDuplicates.isEmpty();
        } catch (Exception e) {
            log.warn("Error checking for duplicate transactions for: {}", transaction.getRequestId(), e);
            return false;
        }
    }
    
    private Map<String, Object> calculatePeriodVolume(Wallet wallet, Instant startDate, Instant endDate) {
        Map<String, Object> volumeData = new HashMap<>();
        
        try {
            Page<Transaction> transactions = transactionRepository.findByWalletAndDateRange(
                    wallet, startDate, endDate, Pageable.unpaged());
            
            BigDecimal totalInflow = BigDecimal.ZERO;
            BigDecimal totalOutflow = BigDecimal.ZERO;
            int transactionCount = 0;
            
            for (Transaction transaction : transactions.getContent()) {
                if (transaction.getStatus() == Status.ACTIVE) {
                    transactionCount++;
                    
                    if (transaction.getReceiverWallet().equals(wallet)) {
                        totalInflow = totalInflow.add(transaction.getAmount());
                    }
                    if (transaction.getSenderWallet() != null && transaction.getSenderWallet().equals(wallet)) {
                        totalOutflow = totalOutflow.add(transaction.getAmount());
                    }
                }
            }
            
            volumeData.put("totalInflow", totalInflow);
            volumeData.put("totalOutflow", totalOutflow);
            volumeData.put("netFlow", totalInflow.subtract(totalOutflow));
            volumeData.put("transactionCount", transactionCount);
            volumeData.put("startDate", startDate);
            volumeData.put("endDate", endDate);
            
        } catch (Exception e) {
            log.error("Error calculating period volume for wallet: {}", wallet.getAccountNumber(), e);
            volumeData.put("error", "Failed to calculate volume: " + e.getMessage());
        }
        
        return volumeData;
    }
}