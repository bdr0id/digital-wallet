package com.boit_droid.wallet.repository;

import com.boit_droid.wallet.entity.Transaction;
import com.boit_droid.wallet.entity.Wallet;
import com.boit_droid.wallet.entity.enums.Status;
import com.boit_droid.wallet.entity.enums.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Find by unique identifier
    Optional<Transaction> findByRequestId(String requestId);

    // Check existence by request ID
    boolean existsByRequestId(String requestId);

    // Find by wallet (sender or receiver)
    List<Transaction> findBySenderWalletOrReceiverWalletOrderByCreatedAtDesc(Wallet senderWallet, Wallet receiverWallet);
    Page<Transaction> findBySenderWalletOrReceiverWalletOrderByCreatedAtDesc(Wallet senderWallet, Wallet receiverWallet, Pageable pageable);

    // Find by sender wallet
    List<Transaction> findBySenderWalletOrderByCreatedAtDesc(Wallet senderWallet);
    Page<Transaction> findBySenderWalletOrderByCreatedAtDesc(Wallet senderWallet, Pageable pageable);

    // Find by receiver wallet
    List<Transaction> findByReceiverWalletOrderByCreatedAtDesc(Wallet receiverWallet);
    Page<Transaction> findByReceiverWalletOrderByCreatedAtDesc(Wallet receiverWallet, Pageable pageable);

    // Find by transaction type
    List<Transaction> findByType(TransactionType type);
    Page<Transaction> findByType(TransactionType type, Pageable pageable);

    // Find by status
    List<Transaction> findByStatus(Status status);
    Page<Transaction> findByStatus(Status status, Pageable pageable);

    // Find by currency
    List<Transaction> findByCurrency(String currency);
    Page<Transaction> findByCurrency(String currency, Pageable pageable);

    // Find by amount range
    @Query("SELECT t FROM Transaction t WHERE t.amount BETWEEN :minAmount AND :maxAmount ORDER BY t.createdAt DESC")
    List<Transaction> findByAmountBetween(@Param("minAmount") BigDecimal minAmount, 
                                         @Param("maxAmount") BigDecimal maxAmount);

    // Find transactions within date range
    @Query("SELECT t FROM Transaction t WHERE t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    Page<Transaction> findByCreatedAtBetween(@Param("startDate") Instant startDate,
                                            @Param("endDate") Instant endDate,
                                            Pageable pageable);

    // Find pending transactions
    @Query("SELECT t FROM Transaction t WHERE t.status = 'PENDING' ORDER BY t.createdAt ASC")
    List<Transaction> findPendingTransactions();

    // Find completed transactions
    @Query("SELECT t FROM Transaction t WHERE t.status = 'ACTIVE' ORDER BY t.completedAt DESC")
    List<Transaction> findCompletedTransactions();

    // Find failed transactions
    @Query("SELECT t FROM Transaction t WHERE t.status = 'CANCELLED' ORDER BY t.createdAt DESC")
    List<Transaction> findFailedTransactions();

    // Find transactions requiring retry
    @Query("SELECT t FROM Transaction t WHERE t.status = 'CANCELLED' AND t.retryCount < t.maxRetries")
    List<Transaction> findTransactionsForRetry();

    // Find by external reference
    Optional<Transaction> findByExternalReference(String externalReference);
    List<Transaction> findByExternalReferenceContaining(String externalReference);

    // Find by batch ID
    List<Transaction> findByBatchId(String batchId);

    // Find by correlation ID
    List<Transaction> findByCorrelationId(String correlationId);

    // Find by parent transaction
    List<Transaction> findByParentTransactionId(String parentTransactionId);

    // Find unverified transactions
    @Query("SELECT t FROM Transaction t WHERE t.verified = false ORDER BY t.createdAt ASC")
    List<Transaction> findUnverifiedTransactions();

    // Find transactions requiring approval
    @Query("SELECT t FROM Transaction t WHERE t.requiresApproval = true AND t.approvedAt IS NULL")
    List<Transaction> findTransactionsRequiringApproval();

    // Find by channel
    List<Transaction> findByChannel(String channel);

    // Find by initiated by
    List<Transaction> findByInitiatedBy(String initiatedBy);

    // Find by processing node
    List<Transaction> findByProcessingNode(String processingNode);

    // Count transactions by status
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.status = :status")
    Long countByStatus(@Param("status") Status status);

    // Count transactions by type
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.type = :type")
    Long countByType(@Param("type") TransactionType type);

    // Count transactions by wallet
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.senderWallet = :wallet OR t.receiverWallet = :wallet")
    Long countByWallet(@Param("wallet") Wallet wallet);

    // Sum transaction amounts by wallet and type
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.receiverWallet = :wallet AND t.type = :type AND t.status = 'ACTIVE'")
    BigDecimal sumAmountByReceiverWalletAndType(@Param("wallet") Wallet wallet, @Param("type") TransactionType type);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.senderWallet = :wallet AND t.type = :type AND t.status = 'ACTIVE'")
    BigDecimal sumAmountBySenderWalletAndType(@Param("wallet") Wallet wallet, @Param("type") TransactionType type);

    // Find transactions by wallet within date range
    @Query("SELECT t FROM Transaction t WHERE " +
           "(t.senderWallet = :wallet OR t.receiverWallet = :wallet) AND " +
           "t.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY t.createdAt DESC")
    Page<Transaction> findByWalletAndDateRange(@Param("wallet") Wallet wallet,
                                              @Param("startDate") Instant startDate,
                                              @Param("endDate") Instant endDate,
                                              Pageable pageable);

    // Find high-value transactions
    @Query("SELECT t FROM Transaction t WHERE t.amount >= :threshold ORDER BY t.amount DESC")
    List<Transaction> findHighValueTransactions(@Param("threshold") BigDecimal threshold);

    // Find transactions with errors
    @Query("SELECT t FROM Transaction t WHERE t.errorMessage IS NOT NULL OR t.errorCode IS NOT NULL")
    List<Transaction> findTransactionsWithErrors();

    // Find recent transactions by wallet
    @Query("SELECT t FROM Transaction t WHERE " +
           "(t.senderWallet = :wallet OR t.receiverWallet = :wallet) AND " +
           "t.createdAt >= :since " +
           "ORDER BY t.createdAt DESC")
    List<Transaction> findRecentTransactionsByWallet(@Param("wallet") Wallet wallet, 
                                                    @Param("since") Instant since);

    // Find transactions by regulatory code
    List<Transaction> findByRegulatoryCode(String regulatoryCode);

    // Find transactions with notifications not sent
    @Query("SELECT t FROM Transaction t WHERE t.senderNotified = false OR t.receiverNotified = false")
    List<Transaction> findTransactionsWithPendingNotifications();

    // Find transactions by device ID
    List<Transaction> findByDeviceId(String deviceId);

    // Find transactions by source IP
    List<Transaction> findBySourceIp(String sourceIp);

    // Complex search query for transactions
    @Query("SELECT t FROM Transaction t WHERE " +
           "(:walletId IS NULL OR t.senderWallet.id = :walletId OR t.receiverWallet.id = :walletId) AND " +
           "(:type IS NULL OR t.type = :type) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:currency IS NULL OR t.currency = :currency) AND " +
           "(:minAmount IS NULL OR t.amount >= :minAmount) AND " +
           "(:maxAmount IS NULL OR t.amount <= :maxAmount) AND " +
           "(:startDate IS NULL OR t.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR t.createdAt <= :endDate) AND " +
           "(:channel IS NULL OR t.channel = :channel) " +
           "ORDER BY t.createdAt DESC")
    Page<Transaction> searchTransactions(@Param("walletId") Long walletId,
                                        @Param("type") TransactionType type,
                                        @Param("status") Status status,
                                        @Param("currency") String currency,
                                        @Param("minAmount") BigDecimal minAmount,
                                        @Param("maxAmount") BigDecimal maxAmount,
                                        @Param("startDate") Instant startDate,
                                        @Param("endDate") Instant endDate,
                                        @Param("channel") String channel,
                                        Pageable pageable);

    // Find duplicate transactions (same amount, wallets, and close timestamps)
    @Query("SELECT t FROM Transaction t WHERE " +
           "t.senderWallet = :senderWallet AND " +
           "t.receiverWallet = :receiverWallet AND " +
           "t.amount = :amount AND " +
           "t.createdAt BETWEEN :startTime AND :endTime AND " +
           "t.id != :excludeId")
    List<Transaction> findPotentialDuplicates(@Param("senderWallet") Wallet senderWallet,
                                             @Param("receiverWallet") Wallet receiverWallet,
                                             @Param("amount") BigDecimal amount,
                                             @Param("startTime") Instant startTime,
                                             @Param("endTime") Instant endTime,
                                             @Param("excludeId") Long excludeId);

    // Find transactions by creator for audit
    List<Transaction> findByCreatedBy(String createdBy);

    // Find soft deleted transactions
    @Query("SELECT t FROM Transaction t WHERE t.isDeleted = true")
    List<Transaction> findDeletedTransactions();

    // Calculate daily transaction volume by wallet
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE " +
           "(t.senderWallet = :wallet OR t.receiverWallet = :wallet) AND " +
           "t.status = 'ACTIVE' AND " +
           "DATE(t.createdAt) = DATE(:date)")
    BigDecimal calculateDailyVolumeByWallet(@Param("wallet") Wallet wallet, @Param("date") Instant date);

    // Calculate monthly transaction volume by wallet
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE " +
           "(t.senderWallet = :wallet OR t.receiverWallet = :wallet) AND " +
           "t.status = 'ACTIVE' AND " +
           "YEAR(t.createdAt) = YEAR(:date) AND MONTH(t.createdAt) = MONTH(:date)")
    BigDecimal calculateMonthlyVolumeByWallet(@Param("wallet") Wallet wallet, @Param("date") Instant date);

    // Additional methods for database constraint validation
    
    // Find transactions with invalid wallet references
    @Query("SELECT t FROM Transaction t WHERE " +
           "(t.senderWallet IS NOT NULL AND t.senderWallet.id NOT IN (SELECT w.id FROM Wallet w)) OR " +
           "(t.receiverWallet IS NOT NULL AND t.receiverWallet.id NOT IN (SELECT w.id FROM Wallet w))")
    List<Transaction> findTransactionsWithInvalidWallets();

    // Find transactions by wallet and status with ordering
    List<Transaction> findByReceiverWalletAndStatusOrderByCreatedAtAsc(Wallet receiverWallet, Status status);
    List<Transaction> findBySenderWalletAndStatusOrderByCreatedAtAsc(Wallet senderWallet, Status status);

    // Find transactions with missing required fields
    @Query("SELECT t FROM Transaction t WHERE " +
           "t.amount IS NULL OR t.amount <= 0 OR " +
           "t.currency IS NULL OR t.currency = '' OR " +
           "t.type IS NULL OR t.status IS NULL OR " +
           "t.receiverWallet IS NULL")
    List<Transaction> findTransactionsWithMissingRequiredFields();

    // Find transactions that exceed wallet limits
    @Query("SELECT t FROM Transaction t WHERE " +
           "t.senderWallet IS NOT NULL AND " +
           "t.senderWallet.dailyTransactionLimit IS NOT NULL AND " +
           "t.amount > t.senderWallet.dailyTransactionLimit")
    List<Transaction> findTransactionsExceedingWalletLimits();

    // Find transactions with inconsistent balance tracking
    @Query("SELECT t FROM Transaction t WHERE " +
           "t.senderBalanceBefore IS NOT NULL AND t.senderBalanceAfter IS NOT NULL AND " +
           "t.senderBalanceBefore - t.amount != t.senderBalanceAfter")
    List<Transaction> findTransactionsWithInconsistentBalanceTracking();

    boolean existsByExternalReference(@NotBlank(message = "M-Pesa transaction ID is required") @Size(min = 10, max = 50, message = "M-Pesa transaction ID must be between 10 and 50 characters") String mpesaTransactionId);
}