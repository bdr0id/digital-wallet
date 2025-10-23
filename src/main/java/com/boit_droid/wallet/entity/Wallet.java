package com.boit_droid.wallet.entity;

import com.boit_droid.wallet.entity.audit.DateAudit;
import com.boit_droid.wallet.entity.enums.Status;
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.*;
import org.hibernate.annotations.Cache;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true, exclude = {"user", "sentTransactions", "receivedTransactions"})
@ToString(callSuper = true, exclude = {"user", "sentTransactions", "receivedTransactions"})
@Getter
@Setter
@NoArgsConstructor
//@SQLDelete(sql = "UPDATE wallets SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
//@FilterDef(name = "deletedFilter", parameters = @ParamDef(name = "isDeleted", type = Boolean.class))
//@Filter(name = "deletedFilter", condition = "is_deleted = :isDeleted")
@Entity
@Table(name = "wallets", 
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_wallet_account_number", columnNames = {"accountNumber"}),
           @UniqueConstraint(name = "uk_wallet_request_id", columnNames = {"requestId"})
       },
       indexes = {
           @Index(name = "idx_wallet_user_id", columnList = "user_id"),
           @Index(name = "idx_wallet_status", columnList = "status"),
           @Index(name = "idx_wallet_account_number", columnList = "accountNumber"),
           @Index(name = "idx_wallet_request_id", columnList = "requestId"),
           @Index(name = "idx_wallet_currency", columnList = "currency"),
           @Index(name = "idx_wallet_balance", columnList = "balance"),
           @Index(name = "idx_wallet_last_transaction", columnList = "lastTransactionAt"),
           @Index(name = "idx_wallet_pin_locked", columnList = "pinLockedUntil"),
           @Index(name = "idx_wallet_composite_status", columnList = "user_id, status, currency"),
           @Index(name = "idx_wallet_active_balance", columnList = "status, balance, currency")
       })
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Wallet extends DateAudit implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_wallet_user"))
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private User user;

    @NotBlank(message = "Request ID is required")
    @Size(max = 36, message = "Request ID must not exceed 36 characters")
    @Column(nullable = false, unique = true, length = 36)
    private String requestId;

    @Size(max = 255, message = "Account name must not exceed 255 characters")
    @Column(length = 255)
    private String accountName;

    @NotBlank(message = "Account number is required")
    @Size(min = 10, max = 20, message = "Account number must be between 10 and 20 characters")
    @Pattern(regexp = "^[0-9A-Z]+$", message = "Account number can only contain digits and uppercase letters")
    @Column(nullable = false, unique = true, length = 20)
    private String accountNumber;

    @NotNull(message = "Balance is required")
    @DecimalMin(value = "0.00", message = "Balance cannot be negative")
    @Digits(integer = 17, fraction = 2, message = "Balance must have at most 17 integer digits and 2 decimal places")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter ISO code")
    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status; // ACTIVE, PENDING, LOCKED, CLOSED

    @Column(nullable = false, length = 255)
    private String salt; // For cryptographic purposes

    @Column(nullable = false, length = 512)
    private String signature; // For verifying transactions

    // PIN management
    @Column(length = 255)
    private String pinHash; // Encrypted PIN

    @Column(length = 255)
    private String pinSalt; // Salt for PIN encryption

    private Instant pinLastUpdated;

    @Column(nullable = false)
    private Integer pinAttempts = 0; // Failed PIN attempts counter

    private Instant pinLockedUntil; // PIN lockout timestamp

    // Wallet limits and settings
    @Column(precision = 19, scale = 2)
    private BigDecimal dailyTransactionLimit;

    @Column(precision = 19, scale = 2)
    private BigDecimal monthlyTransactionLimit;

    @Column(precision = 19, scale = 2)
    private BigDecimal maxBalance;

    @Column(precision = 19, scale = 2)
    private BigDecimal minBalance;

    // Audit trail fields
    @Column(length = 100)
    private String createdBy;

    @Column(length = 100)
    private String lastModifiedBy;

    private Instant lastTransactionAt;

    @Column(length = 50)
    private String lastTransactionIp;

    @Column(length = 100)
    private String statusChangedBy;

    private Instant statusChangedAt;

    @Column(length = 500)
    private String statusChangeReason;

    // Relationships with enhanced configuration
    @OneToMany(mappedBy = "senderWallet", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @OrderBy("createdAt DESC")
    private List<Transaction> sentTransactions = new ArrayList<>();

    @OneToMany(mappedBy = "receiverWallet", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @OrderBy("createdAt DESC")
    private List<Transaction> receivedTransactions = new ArrayList<>();

//    @Column(name = "is_deleted", nullable = false)
//    private boolean isDeleted = false;
//
//    @Column(name = "deleted_at")
//    private Instant deletedAt;

    // Helper methods for relationship management
    public void addSentTransaction(Transaction transaction) {
        sentTransactions.add(transaction);
        transaction.setSenderWallet(this);
    }

    public void removeSentTransaction(Transaction transaction) {
        sentTransactions.remove(transaction);
        transaction.setSenderWallet(null);
    }

    public void addReceivedTransaction(Transaction transaction) {
        receivedTransactions.add(transaction);
        transaction.setReceiverWallet(this);
    }

    public void removeReceivedTransaction(Transaction transaction) {
        receivedTransactions.remove(transaction);
        transaction.setReceiverWallet(null);
    }

    // Business logic helper methods
    public boolean isPinLocked() {
        return pinLockedUntil != null && pinLockedUntil.isAfter(Instant.now());
    }

    public boolean isActive() {
        return Status.ACTIVE.equals(this.status);
    }

    public boolean canTransact() {
        return isActive() && !isPinLocked();
    }

    public String getWalletId() {
        return String.valueOf(id);
    }
}
