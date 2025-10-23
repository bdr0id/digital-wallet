package com.boit_droid.wallet.entity;

import com.boit_droid.wallet.entity.audit.DateAudit;
import com.boit_droid.wallet.entity.enums.Status;
import com.boit_droid.wallet.entity.enums.NotificationType;
import com.boit_droid.wallet.entity.enums.Priority;
import jakarta.persistence.*;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.*;
import org.hibernate.annotations.Cache;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true, exclude = {"user"})
@ToString(callSuper = true, exclude = {"user"})
@Getter
@Setter
@NoArgsConstructor
//@SQLDelete(sql = "UPDATE notifications SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
//@FilterDef(name = "deletedFilter", parameters = @ParamDef(name = "isDeleted", type = Boolean.class))
//@Filter(name = "deletedFilter", condition = "is_deleted = :isDeleted")
@Entity
@Table(name = "notifications", 
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_notification_request_id", columnNames = {"requestId"})
       },
       indexes = {
           @Index(name = "idx_notification_user_id", columnList = "user_id"),
           @Index(name = "idx_notification_status", columnList = "status"),
           @Index(name = "idx_notification_type", columnList = "type"),
           @Index(name = "idx_notification_priority", columnList = "priority"),
           @Index(name = "idx_notification_created_at", columnList = "createdAt"),
           @Index(name = "idx_notification_transaction", columnList = "relatedTransactionId"),
           @Index(name = "idx_notification_composite", columnList = "user_id, status, type"),
           @Index(name = "idx_notification_request_id", columnList = "requestId"),
           @Index(name = "idx_notification_read_status", columnList = "user_id, isRead, createdAt"),
           @Index(name = "idx_notification_delivery", columnList = "isDelivered, sentAt"),
           @Index(name = "idx_notification_expiry", columnList = "isExpired, expiredAt"),
           @Index(name = "idx_notification_retry", columnList = "retryCount, nextRetryAt"),
           @Index(name = "idx_notification_channel", columnList = "channel, status"),
           @Index(name = "idx_notification_template", columnList = "templateId"),
           @Index(name = "idx_notification_campaign", columnList = "campaignId, batchId"),
           @Index(name = "idx_notification_acknowledgment", columnList = "requiresAcknowledgment, acknowledgedAt")
       })
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Notification extends DateAudit implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Request ID is required")
    @Size(max = 36, message = "Request ID must not exceed 36 characters")
    @Column(nullable = false, unique = true, length = 36)
    private String requestId;

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_notification_user"))
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private User user;

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    @Column(nullable = false, length = 255)
    private String title;

    @NotBlank(message = "Message is required")
    @Size(min = 1, max = 1000, message = "Message must be between 1 and 1000 characters")
    @Column(nullable = false, length = 1000)
    private String message;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status; // PENDING, ACTIVE (SENT), CANCELLED, etc.

    @NotNull(message = "Type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type; // TRANSACTION, KYC, SECURITY, PROMOTION, SYSTEM, etc.

    // Enhanced notification fields
    @NotNull(message = "Priority is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priority priority; // LOW, MEDIUM, HIGH, URGENT

    @Column(length = 50)
    private String channel; // EMAIL, SMS, PUSH, IN_APP

    @Column(length = 36)
    private String relatedTransactionId; // Link to related transaction

    @Column(length = 36)
    private String relatedWalletId; // Link to related wallet

    // Delivery tracking
    private Instant sentAt;

    private Instant deliveredAt;

    private Instant readAt;

    private Instant expiredAt;

    @Column(nullable = false)
    private Boolean isRead = false;

    @Column(nullable = false)
    private Boolean isDelivered = false;

    @Column(nullable = false)
    private Boolean isExpired = false;

    // Retry mechanism for failed notifications
    @Column(nullable = false)
    private Integer retryCount = 0;

    @Column(nullable = false)
    private Integer maxRetries = 3;

    private Instant lastRetryAt;

    private Instant nextRetryAt;

    // Error handling
    @Column(length = 1000)
    private String errorMessage;

    @Column(length = 100)
    private String errorCode;

    // Template and personalization
    @Column(length = 100)
    private String templateId; // Reference to notification template

    @Column(length = 2000)
    private String templateData; // JSON data for template personalization

    @Column(length = 10)
    private String language; // Language code for localization

    // Audit trail fields
    @Column(length = 100)
    private String createdBy; // System or user who created the notification

    @Column(length = 100)
    private String lastModifiedBy;

    @Column(length = 50)
    private String sourceIp; // IP address of notification creator

    @Column(length = 100)
    private String deviceId; // Device that triggered the notification

    // Compliance and tracking
    @Column(length = 100)
    private String campaignId; // For marketing/promotional notifications

    @Column(length = 100)
    private String batchId; // For bulk notifications

    @Column(nullable = false)
    private Boolean requiresAcknowledgment = false; // Whether user must acknowledge

    private Instant acknowledgedAt;

    @Column(length = 500)
    private String acknowledgmentData; // Additional data from user acknowledgment

    // External system integration
    @Column(length = 100)
    private String externalReference; // Reference in external notification system

    @Column(length = 100)
    private String externalStatus; // Status from external notification provider

//    @Column(name = "is_deleted", nullable = false)
//    private boolean isDeleted = false;
//
//    @Column(name = "deleted_at")
//    private Instant deletedAt;

    private Instant externalStatusUpdatedAt;
}
