package com.boit_droid.wallet.repository;

import com.boit_droid.wallet.entity.Notification;
import com.boit_droid.wallet.entity.User;
import com.boit_droid.wallet.entity.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Find by unique identifier
    Optional<Notification> findByRequestId(String requestId);

    // Check existence by request ID
    boolean existsByRequestId(String requestId);

    // Find by user
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    // Find by user and status
    List<Notification> findByUserAndStatus(User user, Status status);
    Page<Notification> findByUserAndStatus(User user, Status status, Pageable pageable);

    // Find by user and type
    List<Notification> findByUserAndType(User user, String type);
    Page<Notification> findByUserAndType(User user, String type, Pageable pageable);

    // Find unread notifications
    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.isRead = false ORDER BY n.createdAt DESC")
    List<Notification> findUnreadNotificationsByUser(@Param("user") User user);

    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.isRead = false ORDER BY n.createdAt DESC")
    Page<Notification> findUnreadNotificationsByUser(@Param("user") User user, Pageable pageable);

    // Find by status
    List<Notification> findByStatus(Status status);
    Page<Notification> findByStatus(Status status, Pageable pageable);

    // Find by type
    List<Notification> findByType(String type);
    Page<Notification> findByType(String type, Pageable pageable);

    // Find by priority
    List<Notification> findByPriority(String priority);
    Page<Notification> findByPriority(String priority, Pageable pageable);

    // Find high priority notifications
    @Query("SELECT n FROM Notification n WHERE n.priority IN ('HIGH', 'URGENT') ORDER BY n.createdAt DESC")
    List<Notification> findHighPriorityNotifications();

    // Find by channel
    List<Notification> findByChannel(String channel);

    // Find by related transaction
    List<Notification> findByRelatedTransactionId(String transactionId);

    // Find by related wallet
    List<Notification> findByRelatedWalletId(String walletId);

    // Find notifications within date range
    @Query("SELECT n FROM Notification n WHERE n.createdAt BETWEEN :startDate AND :endDate ORDER BY n.createdAt DESC")
    Page<Notification> findByCreatedAtBetween(@Param("startDate") Instant startDate,
                                             @Param("endDate") Instant endDate,
                                             Pageable pageable);

    // Find delivered notifications
    @Query("SELECT n FROM Notification n WHERE n.isDelivered = true ORDER BY n.deliveredAt DESC")
    List<Notification> findDeliveredNotifications();

    // Find expired notifications
    @Query("SELECT n FROM Notification n WHERE n.isExpired = true OR n.expiredAt <= :currentTime")
    List<Notification> findExpiredNotifications(@Param("currentTime") Instant currentTime);

    // Find failed notifications (for retry)
    @Query("SELECT n FROM Notification n WHERE n.status = 'CANCELLED' AND n.retryCount < n.maxRetries")
    List<Notification> findFailedNotificationsForRetry();

    // Find notifications requiring acknowledgment
    @Query("SELECT n FROM Notification n WHERE n.requiresAcknowledgment = true AND n.acknowledgedAt IS NULL")
    List<Notification> findNotificationsRequiringAcknowledgment();

    // Find by template ID
    List<Notification> findByTemplateId(String templateId);

    // Find by campaign ID
    List<Notification> findByCampaignId(String campaignId);

    // Find by batch ID
    List<Notification> findByBatchId(String batchId);

    // Count unread notifications by user
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.user = :user AND n.isRead = false")
    Long countUnreadNotificationsByUser(@Param("user") User user);

    // Count notifications by status
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.status = :status")
    Long countByStatus(@Param("status") Status status);

    // Count notifications by type
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.type = :type")
    Long countByType(@Param("type") String type);

    // Count notifications by priority
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.priority = :priority")
    Long countByPriority(@Param("priority") String priority);

    // Find notifications by user and read status
    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.isRead = :isRead ORDER BY n.createdAt DESC")
    Page<Notification> findByUserAndReadStatus(@Param("user") User user, 
                                              @Param("isRead") Boolean isRead, 
                                              Pageable pageable);

    // Find recent notifications by user
    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.createdAt >= :since ORDER BY n.createdAt DESC")
    List<Notification> findRecentNotificationsByUser(@Param("user") User user, 
                                                     @Param("since") Instant since);

    // Find notifications by external reference
    Optional<Notification> findByExternalReference(String externalReference);

    // Find notifications with errors
    @Query("SELECT n FROM Notification n WHERE n.errorMessage IS NOT NULL OR n.errorCode IS NOT NULL")
    List<Notification> findNotificationsWithErrors();

    // Find notifications by language
    List<Notification> findByLanguage(String language);

    // Find notifications by creator for audit
    List<Notification> findByCreatedBy(String createdBy);

    // Find soft deleted notifications
    @Query("SELECT n FROM Notification n WHERE n.isDeleted = true")
    List<Notification> findDeletedNotifications();

    // Complex search query
    @Query("SELECT n FROM Notification n WHERE " +
           "(:userId IS NULL OR n.user.id = :userId) AND " +
           "(:status IS NULL OR n.status = :status) AND " +
           "(:type IS NULL OR n.type = :type) AND " +
           "(:priority IS NULL OR n.priority = :priority) AND " +
           "(:isRead IS NULL OR n.isRead = :isRead) AND " +
           "(:startDate IS NULL OR n.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR n.createdAt <= :endDate) " +
           "ORDER BY n.createdAt DESC")
    Page<Notification> searchNotifications(@Param("userId") Long userId,
                                          @Param("status") Status status,
                                          @Param("type") String type,
                                          @Param("priority") String priority,
                                          @Param("isRead") Boolean isRead,
                                          @Param("startDate") Instant startDate,
                                          @Param("endDate") Instant endDate,
                                          Pageable pageable);
}
