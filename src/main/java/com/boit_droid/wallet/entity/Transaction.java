package com.boit_droid.wallet.entity;

import com.boit_droid.wallet.entity.audit.DateAudit;
import com.boit_droid.wallet.entity.enums.Status;
import com.boit_droid.wallet.entity.enums.TransactionType;
import jakarta.persistence.*;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.*;
import org.hibernate.annotations.Cache;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true, exclude = {"senderWallet", "receiverWallet"})
@ToString(callSuper = true, exclude = {"senderWallet", "receiverWallet"})
@Getter
@Setter
@NoArgsConstructor
//@SQLDelete(sql = "UPDATE transactions SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
//@FilterDef(name = "deletedFilter", parameters = @ParamDef(name = "isDeleted", type = Boolean.class))
//@Filter(name = "deletedFilter", condition = "is_deleted = :isDeleted")
@Entity
@Table(name = "transactions", 
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_transaction_request_id", columnNames = {"requestId"})
       },
       indexes = {
           @Index(name = "idx_transaction_request_id", columnList = "requestId"),
           @Index(name = "idx_transaction_type", columnList = "type"),
           @Index(name = "idx_transaction_status", columnList = "status"),
           @Index(name = "idx_transaction_sender", columnList = "sender_wallet_id"),
           @Index(name = "idx_transaction_receiver", columnList = "receiver_wallet_id"),
           @Index(name = "idx_transaction_created_at", columnList = "createdAt"),
           @Index(name = "idx_transaction_amount", columnList = "amount"),
           @Index(name = "idx_transaction_reference", columnList = "externalReference"),
           @Index(name = "idx_transaction_batch", columnList = "batchId"),
           @Index(name = "idx_transaction_composite", columnList = "status, type, createdAt"),
           @Index(name = "idx_transaction_processed_at", columnList = "processedAt"),
           @Index(name = "idx_transaction_completed_at", columnList = "completedAt"),
           @Index(name = "idx_transaction_correlation", columnList = "correlationId"),
           @Index(name = "idx_transaction_parent", columnList = "parentTransactionId"),
           @Index(name = "idx_transaction_verification", columnList = "verified, verifiedAt"),
           @Index(name = "idx_transaction_approval", columnList = "requiresApproval, approvedAt"),
           @Index(name = "idx_transaction_wallet_date", columnList = "receiver_wallet_id, createdAt"),
           @Index(name = "idx_transaction_sender_date", columnList = "sender_wallet_id, createdAt"),
           @Index(name = "idx_transaction_amount_range", columnList = "amount, currency, type")
       })
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Transaction extends DateAudit implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Request ID is required")
    @Size(max = 36, message = "Request ID must not exceed 36 characters")
    @Column(nullable = false, unique = true, length = 36)
    private String requestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_wallet_id", foreignKey = @ForeignKey(name = "fk_transaction_sender_wallet"))
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Wallet senderWallet; // Wallet sending the money, nullable for deposits

    @NotNull(message = "Receiver wallet is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receiver_wallet_id", nullable = false, foreignKey = @ForeignKey(name = "fk_transaction_receiver_wallet"))
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Wallet receiverWallet; // Wallet receiving the money

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Digits(integer = 17, fraction = 2, message = "Amount must have at most 17 integer digits and 2 decimal places")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter ISO code")
    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type; // DEPOSIT, WITHDRAWAL, TRANSFER, TOP_UP

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status; // PENDING, ACTIVE (COMPLETED), CANCELLED, TIMEOUT

    @Column(length = 500)
    private String description; // Optional description or note for the transaction

    // Enhanced transaction tracking fields
    @Column(length = 100)
    private String externalReference; // External system reference (e.g., M-Pesa transaction ID)

    @Column(length = 36)
    private String batchId; // For batch processing of transactions

    @Column(length = 100)
    private String correlationId; // For tracking related transactions

    @Column(length = 36)
    private String parentTransactionId; // For linked transactions (reversals, refunds)

    // Balance tracking for audit
    @Column(precision = 19, scale = 2)
    private BigDecimal senderBalanceBefore;

    @Column(precision = 19, scale = 2)
    private BigDecimal senderBalanceAfter;

    @Column(precision = 19, scale = 2)
    private BigDecimal receiverBalanceBefore;

    @Column(precision = 19, scale = 2)
    private BigDecimal receiverBalanceAfter;

    // Transaction fees and charges
    @Column(precision = 19, scale = 2)
    private BigDecimal transactionFee;

    @Column(precision = 19, scale = 2)
    private BigDecimal exchangeRate; // For currency conversion

    // Processing details
    private Instant processedAt;

    private Instant completedAt;

    @Column(length = 100)
    private String processedBy; // System or user who processed the transaction

    @Column(length = 50)
    private String processingNode; // Server/node that processed the transaction

    // Error handling and retry mechanism
    @Column(length = 1000)
    private String errorMessage;

    @Column(length = 100)
    private String errorCode;

    @Column(nullable = false)
    private Integer retryCount = 0;

    private Instant lastRetryAt;

    @Column(nullable = false)
    private Integer maxRetries = 3;

    // Security and verification
    @Column(length = 512)
    private String transactionSignature; // Cryptographic signature for verification

    @Column(length = 255)
    private String transactionHash; // Hash of transaction data for integrity

    @Column(nullable = false)
    private Boolean verified = false; // Whether transaction has been verified

    private Instant verifiedAt;

    @Column(length = 100)
    private String verifiedBy;

    // Audit trail fields
    @Column(length = 100)
    private String initiatedBy; // User or system that initiated the transaction

    @Column(length = 50)
    private String sourceIp; // IP address of transaction initiator

    @Column(length = 255)
    private String userAgent; // User agent string for web transactions

    @Column(length = 100)
    private String deviceId; // Device identifier for mobile transactions

    @Column(length = 50)
    private String channel; // WEB, MOBILE, API, USSD, etc.

    @Column(length = 100)
    private String createdBy;

    @Column(length = 100)
    private String lastModifiedBy;

    // Compliance and regulatory fields
    @Column(length = 100)
    private String regulatoryCode; // For regulatory reporting

    @Column(nullable = false)
    private Boolean requiresApproval = false;

    @Column(length = 100)
    private String approvedBy;

    private Instant approvedAt;

    @Column(length = 500)
    private String approvalComments;

    // Notification tracking
    @Column(nullable = false)
    private Boolean senderNotified = false;

    @Column(nullable = false)
    private Boolean receiverNotified = false;

    private Instant senderNotifiedAt;

    private Instant receiverNotifiedAt;

//    @Column(name = "is_deleted", nullable = false)
//    private boolean isDeleted = false;
//
//    @Column(name = "deleted_at")
//    private Instant deletedAt;

    public Long getTransactionId() {
        return id;
    }

    public Instant getLastModifiedAt() {
        return getLastModifiedAt();
    }
}
