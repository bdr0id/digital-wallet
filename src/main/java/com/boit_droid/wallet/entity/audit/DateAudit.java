package com.boit_droid.wallet.entity.audit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.Instant;

@Setter
@Getter
@MappedSuperclass
@EntityListeners({AuditingEntityListener.class, AuditTrailEntityListener.class})
@JsonIgnoreProperties(value = {"createdAt","updatedAt","version"},allowGetters = true)
public abstract class DateAudit implements Serializable {
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    // Optimistic locking for concurrent access control
    @Version
    @Column(nullable = false)
    private Long version = 0L;

    // Enhanced audit trail fields
    @Column(length = 36)
    private String auditTrailId; // Unique identifier for audit trail tracking

    @Column(length = 100)
    private String operationType; // CREATE, UPDATE, DELETE, etc.

    @Column(length = 50)
    private String sourceSystem; // System that made the change (WEB, MOBILE, API, BATCH)

    @Column(length = 100)
    private String sessionId; // Session identifier for tracking user sessions

    @Column(length = 2000)
    private String changeDescription; // Description of what changed

    @Column(length = 5000)
    private String previousValues; // JSON of previous values for audit

    @Column(nullable = false)
    private Boolean isDeleted = false; // Soft delete flag

    private Instant deletedAt; // When the record was soft deleted

    @Column(length = 100)
    private String deletedBy; // Who soft deleted the record

    @Column(length = 500)
    private String deletionReason; // Reason for deletion

    // Data integrity fields
    @Column(length = 255)
    private String dataHash; // Hash of critical data for integrity verification

    @Column(length = 255)
    private String checksum; // Checksum for data validation

    // Compliance and regulatory audit fields
    @Column(nullable = false)
    private Boolean requiresApproval = false; // Whether changes require approval

    @Column(nullable = false)
    private Boolean isApproved = false; // Whether changes have been approved

    private Instant approvedAt; // When the changes were approved

    @Column(length = 100)
    private String approvedBy; // Who approved the changes

    @Column(length = 500)
    private String approvalComments; // Comments from approver

    // Archival and retention
    @Column(nullable = false)
    private Boolean isArchived = false; // Whether record is archived

    private Instant archivedAt; // When the record was archived

    @Column(length = 100)
    private String archivedBy; // Who archived the record

    private Instant retentionExpiryDate; // When record can be permanently deleted

    // Data classification and sensitivity
    @Column(length = 50)
    private String dataClassification; // PUBLIC, INTERNAL, CONFIDENTIAL, RESTRICTED

    @Column(nullable = false)
    private Boolean containsPII = false; // Whether record contains personally identifiable information

    @Column(nullable = false)
    private Boolean containsFinancialData = false; // Whether record contains financial data

    // Geographic and jurisdictional tracking
    @Column(length = 10)
    private String jurisdiction; // Legal jurisdiction (country code)

    @Column(length = 100)
    private String dataResidency; // Where data must reside for compliance

    // Performance and monitoring
    @Column(length = 100)
    private String performanceMetrics; // JSON of performance metrics for the operation

    private Long processingTimeMs; // Time taken to process the operation in milliseconds
}
