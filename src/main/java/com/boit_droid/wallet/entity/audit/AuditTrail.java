package com.boit_droid.wallet.entity.audit;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;

@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = false)
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "audit_trail",
       indexes = {
           @Index(name = "idx_audit_entity_type", columnList = "entityType"),
           @Index(name = "idx_audit_entity_id", columnList = "entityId"),
           @Index(name = "idx_audit_operation", columnList = "operation"),
           @Index(name = "idx_audit_user", columnList = "userId"),
           @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
           @Index(name = "idx_audit_session", columnList = "sessionId"),
           @Index(name = "idx_audit_composite", columnList = "entityType, entityId, timestamp"),
           @Index(name = "idx_audit_transaction", columnList = "transactionId")
       })
public class AuditTrail implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 36)
    private String auditId; // Unique identifier for this audit record

    // Entity information
    @Column(nullable = false, length = 100)
    private String entityType; // User, Wallet, Transaction, Notification, etc.

    @Column(nullable = false, length = 36)
    private String entityId; // ID of the entity that was changed

    @Column(nullable = false, length = 50)
    private String operation; // CREATE, UPDATE, DELETE, LOGIN, LOGOUT, etc.

    // User and session information
    @Column(length = 36)
    private String userId; // User who performed the action

    @Column(length = 100)
    private String username; // Username for easier identification

    @Column(length = 100)
    private String sessionId; // Session identifier

    @Column(length = 50)
    private String sourceIp; // IP address of the user/system

    @Column(length = 255)
    private String userAgent; // User agent string

    @Column(length = 100)
    private String deviceId; // Device identifier

    // Timing information
    @Column(nullable = false)
    private Instant timestamp; // When the action occurred

    @Column(nullable = false)
    private Instant createdAt; // When this audit record was created

    private Long processingTimeMs; // Time taken to process the operation

    // Change details
    @Column(length = 2000)
    private String changeDescription; // Human-readable description of the change

    @Column(columnDefinition = "TEXT")
    private String oldValues; // JSON of old values

    @Column(columnDefinition = "TEXT")
    private String newValues; // JSON of new values

    @Column(columnDefinition = "TEXT")
    private String changedFields; // JSON array of field names that changed

    // System and application context
    @Column(length = 50)
    private String sourceSystem; // WEB, MOBILE, API, BATCH, SYSTEM

    @Column(length = 100)
    private String applicationVersion; // Version of the application

    @Column(length = 100)
    private String serverNode; // Server/node that processed the request

    @Column(length = 100)
    private String requestId; // Request ID for correlation

    // Transaction context (for financial operations)
    @Column(length = 36)
    private String transactionId; // Related transaction ID

    @Column(length = 100)
    private String transactionType; // Type of transaction if applicable

    @Column(precision = 19, scale = 2)
    private java.math.BigDecimal transactionAmount; // Amount involved in the transaction

    @Column(length = 3)
    private String currency; // Currency code

    // Security and compliance
    @Column(length = 50)
    private String riskLevel; // LOW, MEDIUM, HIGH, CRITICAL

    @Column(nullable = false)
    private Boolean isSensitiveOperation = false; // Whether this is a sensitive operation

    @Column(nullable = false)
    private Boolean requiresApproval = false; // Whether this operation required approval

    @Column(length = 100)
    private String approvedBy; // Who approved the operation

    private Instant approvedAt; // When the operation was approved

    // Error and exception tracking
    @Column(length = 100)
    private String errorCode; // Error code if operation failed

    @Column(length = 1000)
    private String errorMessage; // Error message if operation failed

    @Column(columnDefinition = "TEXT")
    private String stackTrace; // Stack trace for debugging

    // Business context
    @Column(length = 100)
    private String businessProcess; // Business process this audit relates to

    @Column(length = 100)
    private String workflowId; // Workflow identifier

    @Column(length = 100)
    private String batchId; // Batch identifier for bulk operations

    // Data classification and retention
    @Column(length = 50)
    private String dataClassification; // PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED

    @Column(nullable = false)
    private Boolean containsPII = false; // Contains personally identifiable information

    @Column(nullable = false)
    private Boolean containsFinancialData = false; // Contains financial data

    private Instant retentionExpiryDate; // When this audit record can be deleted

    // Geographic and jurisdictional information
    @Column(length = 10)
    private String jurisdiction; // Legal jurisdiction (country code)

    @Column(length = 100)
    private String dataCenter; // Data center where operation was processed

    @Column(length = 50)
    private String timezone; // Timezone of the operation

    // Additional metadata
    @Column(columnDefinition = "TEXT")
    private String additionalMetadata; // JSON of additional metadata

    @Column(length = 255)
    private String tags; // Comma-separated tags for categorization

    // Correlation and tracing
    @Column(length = 100)
    private String correlationId; // For distributed tracing

    @Column(length = 100)
    private String parentAuditId; // Parent audit record for hierarchical operations

    @Column(length = 36)
    private String rootCauseAuditId; // Root cause audit record for cascading changes

    // Performance metrics
    @Column(length = 500)
    private String performanceMetrics; // JSON of performance metrics

    @Column(nullable = false)
    private Boolean isSuccessful = true; // Whether the operation was successful

    // Notification and alerting
    @Column(nullable = false)
    private Boolean alertGenerated = false; // Whether an alert was generated

    @Column(length = 100)
    private String alertLevel; // NONE, INFO, WARNING, ERROR, CRITICAL

    @Column(length = 500)
    private String alertMessage; // Alert message if generated
}