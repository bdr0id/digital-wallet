package com.boit_droid.wallet.repository;

import com.boit_droid.wallet.entity.User;
import com.boit_droid.wallet.entity.Wallet;
import com.boit_droid.wallet.entity.enums.Status;
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
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    // Find by unique identifiers
    Optional<Wallet> findByRequestId(String requestId);
    Optional<Wallet> findByAccountNumber(String accountNumber);

    // Check existence by unique fields
    boolean existsByAccountNumber(String accountNumber);
    boolean existsByRequestId(String requestId);

    // Find by user
    List<Wallet> findByUser(User user);
    List<Wallet> findByUserOrderByCreatedAtDesc(User user);
    Optional<Wallet> findByUserAndStatus(User user, Status status);

    // Find by status
    List<Wallet> findByStatus(Status status);
    Page<Wallet> findByStatus(Status status, Pageable pageable);

    // Find active wallets
    @Query("SELECT w FROM Wallet w WHERE w.status = 'ACTIVE' ORDER BY w.createdAt DESC")
    List<Wallet> findActiveWallets();

    // Find wallets by currency
    List<Wallet> findByCurrency(String currency);
    Page<Wallet> findByCurrency(String currency, Pageable pageable);

    // Find wallets by balance range
    @Query("SELECT w FROM Wallet w WHERE w.balance BETWEEN :minBalance AND :maxBalance ORDER BY w.balance DESC")
    List<Wallet> findByBalanceBetween(@Param("minBalance") BigDecimal minBalance, 
                                     @Param("maxBalance") BigDecimal maxBalance);

    // Find wallets with balance greater than amount
    @Query("SELECT w FROM Wallet w WHERE w.balance >= :amount AND w.status = 'ACTIVE'")
    List<Wallet> findWalletsWithSufficientBalance(@Param("amount") BigDecimal amount);

    // Find wallets by user status and wallet status
    @Query("SELECT w FROM Wallet w WHERE w.user.status = :userStatus AND w.status = :walletStatus")
    List<Wallet> findByUserStatusAndWalletStatus(@Param("userStatus") Status userStatus, 
                                                 @Param("walletStatus") Status walletStatus);

    // Find wallets requiring PIN reset
    @Query("SELECT w FROM Wallet w WHERE w.pinAttempts >= 3 AND w.pinLockedUntil IS NOT NULL AND w.pinLockedUntil > :currentTime")
    List<Wallet> findLockedWallets(@Param("currentTime") Instant currentTime);

    // Find wallets with recent transactions
    @Query("SELECT w FROM Wallet w WHERE w.lastTransactionAt >= :since ORDER BY w.lastTransactionAt DESC")
    List<Wallet> findWalletsWithRecentActivity(@Param("since") Instant since);

    // Find wallets by creation date range
    @Query("SELECT w FROM Wallet w WHERE w.createdAt BETWEEN :startDate AND :endDate ORDER BY w.createdAt DESC")
    Page<Wallet> findByCreatedAtBetween(@Param("startDate") Instant startDate,
                                       @Param("endDate") Instant endDate,
                                       Pageable pageable);

    // Find wallets exceeding daily limit
    @Query("SELECT w FROM Wallet w WHERE w.dailyTransactionLimit IS NOT NULL AND " +
           "w.balance > w.dailyTransactionLimit")
    List<Wallet> findWalletsExceedingDailyLimit();

    // Count wallets by status
    @Query("SELECT COUNT(w) FROM Wallet w WHERE w.status = :status")
    Long countByStatus(@Param("status") Status status);

    // Count wallets by currency
    @Query("SELECT COUNT(w) FROM Wallet w WHERE w.currency = :currency")
    Long countByCurrency(@Param("currency") String currency);

    // Calculate total balance by currency
    @Query("SELECT SUM(w.balance) FROM Wallet w WHERE w.currency = :currency AND w.status = 'ACTIVE'")
    BigDecimal getTotalBalanceByCurrency(@Param("currency") String currency);

    // Find wallets by account name pattern
    @Query("SELECT w FROM Wallet w WHERE LOWER(w.accountName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Wallet> findByAccountNameContaining(@Param("name") String name);

    // Find wallets with soft delete
    @Query("SELECT w FROM Wallet w WHERE w.isDeleted = true")
    List<Wallet> findDeletedWallets();

    // Find wallets by creator for audit
    List<Wallet> findByCreatedBy(String createdBy);

    // Find wallets requiring approval
    @Query("SELECT w FROM Wallet w WHERE w.requiresApproval = true AND w.isApproved = false")
    List<Wallet> findWalletsRequiringApproval();

    // Find wallets by user KYC status
    @Query("SELECT w FROM Wallet w WHERE w.user.kycStatus = :kycStatus")
    List<Wallet> findByUserKycStatus(@Param("kycStatus") String kycStatus);

    // Find wallets with PIN issues
    @Query("SELECT w FROM Wallet w WHERE w.pinAttempts > 0 OR w.pinLockedUntil IS NOT NULL")
    List<Wallet> findWalletsWithPinIssues();

    // Find wallets by status change date
    @Query("SELECT w FROM Wallet w WHERE w.statusChangedAt >= :since ORDER BY w.statusChangedAt DESC")
    List<Wallet> findWalletsWithRecentStatusChange(@Param("since") Instant since);

    // Additional methods for database constraint validation
    
    // Find orphaned wallets (wallets without valid user reference)
    @Query("SELECT w FROM Wallet w WHERE w.user IS NULL OR w.user.id NOT IN (SELECT u.id FROM User u)")
    List<Wallet> findWalletsWithoutUser();

    // Find wallets with potentially inconsistent balances
    @Query("SELECT w FROM Wallet w WHERE w.balance < 0 OR w.balance IS NULL")
    List<Wallet> findWalletsWithInconsistentBalance();

    // Find wallets by user request ID for better performance
    @Query("SELECT w FROM Wallet w WHERE w.user.requestId = :userRequestId")
    List<Wallet> findByUserRequestId(@Param("userRequestId") String userRequestId);

    // Find wallets with specific transaction limits
    @Query("SELECT w FROM Wallet w WHERE w.dailyTransactionLimit = :limit OR w.monthlyTransactionLimit = :limit")
    List<Wallet> findByTransactionLimit(@Param("limit") BigDecimal limit);

    // Find wallets created by specific system/user for audit
    @Query("SELECT w FROM Wallet w WHERE w.createdBy = :creator ORDER BY w.createdAt DESC")
    List<Wallet> findByCreatedByOrderByCreatedAtDesc(@Param("creator") String creator);
}
