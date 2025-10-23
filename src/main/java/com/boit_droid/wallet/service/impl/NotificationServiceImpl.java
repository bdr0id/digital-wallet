package com.boit_droid.wallet.service.impl;

import com.boit_droid.wallet.dto.request.NotificationRequest;
import com.boit_droid.wallet.dto.response.CustomApiResponse;
import com.boit_droid.wallet.dto.response.NotificationResponse;
import com.boit_droid.wallet.dto.response.PagedResponse;
import com.boit_droid.wallet.entity.Notification;
import com.boit_droid.wallet.entity.User;
import com.boit_droid.wallet.entity.enums.NotificationType;
import com.boit_droid.wallet.entity.enums.Priority;
import com.boit_droid.wallet.entity.enums.Status;
import com.boit_droid.wallet.repository.NotificationRepository;
import com.boit_droid.wallet.repository.UserRepository;
import com.boit_droid.wallet.service.NotificationService;
import com.boit_droid.wallet.service.EmailService;
import com.boit_droid.wallet.util.RequestIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    
    @Value("${app.notification.sms.enabled:true}")
    private boolean smsEnabled;
    
    @Value("${app.notification.email.enabled:true}")
    private boolean emailEnabled;
    
    @Value("${app.notification.push.enabled:true}")
    private boolean pushEnabled;
    
    @Value("${app.notification.retry.max-attempts:3}")
    private int maxRetryAttempts;
    
    @Value("${app.notification.retry.delay-seconds:30}")
    private int retryDelaySeconds;
    
    // Async executor for notification delivery
    private final Executor notificationExecutor = Executors.newFixedThreadPool(10);
    
    // In-memory tracking for delivery status (in production, use Redis or database)
    private final Map<String, DeliveryStatus> deliveryStatusMap = new ConcurrentHashMap<>();
    
    // OTP templates
    private static final Map<String, String> OTP_TEMPLATES = Map.of(
        "TRANSFER", "Your OTP for wallet transfer is: {code}. Valid for 5 minutes. Do not share this code.",
        "PIN_UPDATE", "Your OTP for PIN update is: {code}. Valid for 5 minutes. Do not share this code.",
        "WALLET_STATUS", "Your OTP for wallet status change is: {code}. Valid for 5 minutes. Do not share this code.",
        "WALLET_DELETION", "Your OTP for wallet deletion is: {code}. Valid for 5 minutes. Do not share this code.",
        "KYC", "Your OTP for KYC verification is: {code}. Valid for 5 minutes. Do not share this code.",
        "USER_STATUS", "Your OTP for account status change is: {code}. Valid for 5 minutes. Do not share this code.",
        "DEFAULT", "Your verification code is: {code}. Valid for 5 minutes. Do not share this code."
    );

    @Override
    public CustomApiResponse<NotificationResponse> sendNotification(String userId, NotificationRequest request) {
        try {
            User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
            
            Notification notification = createNotification(user, request);
            notification = notificationRepository.save(notification);
            
            // Send notification asynchronously
            sendNotificationAsync(notification);
            
            NotificationResponse response = mapToResponse(notification);
            return CustomApiResponse.success("Notification sent successfully", response);
            
        } catch (Exception e) {
            log.error("Failed to send notification to user {}: {}", userId, e.getMessage());
            return CustomApiResponse.error("Failed to send notification: " + e.getMessage(), 
                UUID.randomUUID().toString(), List.of(e.getMessage()));
        }
    }

    @Override
    public CustomApiResponse<NotificationResponse> sendTemplatedNotification(String userId, NotificationType type,
                                                                            Priority priority, String templateId,
                                                                            Map<String, Object> templateData) {
        try {
            User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
            
            Notification notification = createTemplatedNotification(user, type, priority, templateId, templateData);
            notification = notificationRepository.save(notification);
            
            // Send notification asynchronously
            sendNotificationAsync(notification);
            
            NotificationResponse response = mapToResponse(notification);
            return CustomApiResponse.success("Templated notification sent successfully", response);
            
        } catch (Exception e) {
            log.error("Failed to send templated notification to user {}: {}", userId, e.getMessage());
            return CustomApiResponse.error("Failed to send templated notification: " + e.getMessage(), 
                UUID.randomUUID().toString(), List.of(e.getMessage()));
        }
    }

    @Override
    public CustomApiResponse<PagedResponse<NotificationResponse>> getNotifications(String userId, int page, int size) {
        try {
            User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
            
            Pageable pageable = PageRequest.of(page, size);
            Page<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
            
            List<NotificationResponse> responses = notifications.getContent().stream()
                .map(this::mapToResponse)
                .toList();
            
            PagedResponse<NotificationResponse> pagedResponse = PagedResponse.of(responses, notifications);
            
            return CustomApiResponse.success("Notifications retrieved successfully", pagedResponse);
            
        } catch (Exception e) {
            log.error("Failed to get notifications for user {}: {}", userId, e.getMessage());
            return CustomApiResponse.error("Failed to retrieve notifications: " + e.getMessage(), 
                UUID.randomUUID().toString(), List.of(e.getMessage()));
        }
    }

    @Override
    public CustomApiResponse<PagedResponse<NotificationResponse>> getUnreadNotifications(String userId, int page, int size) {
        try {
            User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
            
            Pageable pageable = PageRequest.of(page, size);
            Page<Notification> notifications = notificationRepository.findUnreadNotificationsByUser(user, pageable);
            
            List<NotificationResponse> responses = notifications.getContent().stream()
                .map(this::mapToResponse)
                .toList();
            
            PagedResponse<NotificationResponse> pagedResponse = PagedResponse.of(responses, notifications);
            
            return CustomApiResponse.success("Unread notifications retrieved successfully", pagedResponse);
            
        } catch (Exception e) {
            log.error("Failed to get unread notifications for user {}: {}", userId, e.getMessage());
            return CustomApiResponse.error("Failed to retrieve unread notifications: " + e.getMessage(), 
                UUID.randomUUID().toString(), List.of(e.getMessage()));
        }
    }

    @Override
    @Transactional
    public CustomApiResponse<String> markNotificationAsRead(String notificationId) {
        try {
            Notification notification = notificationRepository.findById(Long.valueOf(notificationId))
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
            
            notification.setIsRead(true);
            notification.setReadAt(Instant.now());
            notificationRepository.save(notification);
            
            return CustomApiResponse.success("Notification marked as read", "Success");
            
        } catch (Exception e) {
            log.error("Failed to mark notification as read {}: {}", notificationId, e.getMessage());
            return CustomApiResponse.error("Failed to mark notification as read: " + e.getMessage(), 
                UUID.randomUUID().toString(), List.of(e.getMessage()));
        }
    }

    @Override
    @Transactional
    public CustomApiResponse<String> markAllNotificationsAsRead(String userId) {
        try {
            User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
            
            List<Notification> unreadNotifications = notificationRepository.findUnreadNotificationsByUser(user);
            
            for (Notification notification : unreadNotifications) {
                notification.setIsRead(true);
                notification.setReadAt(Instant.now());
            }
            
            notificationRepository.saveAll(unreadNotifications);
            
            return CustomApiResponse.success("All notifications marked as read", "Success");
            
        } catch (Exception e) {
            log.error("Failed to mark all notifications as read for user {}: {}", userId, e.getMessage());
            return CustomApiResponse.error("Failed to mark all notifications as read: " + e.getMessage(), 
                UUID.randomUUID().toString(), List.of(e.getMessage()));
        }
    }

    @Override
    public CustomApiResponse<Map<String, Long>> getNotificationCounts(String userId) {
        try {
            User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
            
            Long unreadCount = notificationRepository.countUnreadNotificationsByUser(user);
            Long totalCount = (long) notificationRepository.findByUserOrderByCreatedAtDesc(user).size();
            
            Map<String, Long> counts = Map.of(
                "unread", unreadCount,
                "total", totalCount
            );
            
            return CustomApiResponse.success("Notification counts retrieved successfully", counts);
            
        } catch (Exception e) {
            log.error("Failed to get notification counts for user {}: {}", userId, e.getMessage());
            return CustomApiResponse.error("Failed to retrieve notification counts: " + e.getMessage(), 
                UUID.randomUUID().toString(), List.of(e.getMessage()));
        }
    }

    @Override
    @Transactional
    public CustomApiResponse<String> deleteNotification(String notificationId) {
        try {
            Notification notification = notificationRepository.findById(Long.valueOf(notificationId))
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
            
            notificationRepository.delete(notification);
            
            return CustomApiResponse.success("Notification deleted successfully", "Success");
            
        } catch (Exception e) {
            log.error("Failed to delete notification {}: {}", notificationId, e.getMessage());
            return CustomApiResponse.error("Failed to delete notification: " + e.getMessage(), 
                UUID.randomUUID().toString(), List.of(e.getMessage()));
        }
    }

    @Override
    public void sendTransactionNotification(String userId, String transactionId, String walletId, 
                                          String transactionType, double amount, String currency) {
        try {
            User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
            
            String title = "Transaction " + transactionType;
            String message = String.format("Transaction of %s %.2f completed successfully", currency, amount);
            
            Notification notification = createBasicNotification(user, title, message, 
                NotificationType.TRANSACTION, Priority.MEDIUM);
            notification.setRelatedTransactionId(transactionId);
            notification.setRelatedWalletId(walletId);
            
            notification = notificationRepository.save(notification);
            sendNotificationAsync(notification);
            
            log.info("Transaction notification sent to user {} for transaction {}", userId, transactionId);
            
        } catch (Exception e) {
            log.error("Failed to send transaction notification to user {}: {}", userId, e.getMessage());
        }
    }

    @Override
    public void sendKYCNotification(String userId, String kycStatus, String message) {
        try {
            User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
            
            String title = "KYC Status Update";
            
            Notification notification = createBasicNotification(user, title, message, 
                NotificationType.KYC, Priority.HIGH);
            
            notification = notificationRepository.save(notification);
            sendNotificationAsync(notification);
            
            log.info("KYC notification sent to user {} with status {}", userId, kycStatus);
            
        } catch (Exception e) {
            log.error("Failed to send KYC notification to user {}: {}", userId, e.getMessage());
        }
    }

    @Override
    public void sendSecurityNotification(String userId, String securityEvent, String details) {
        try {
            User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
            
            String title = "Security Alert: " + securityEvent;
            
            Notification notification = createBasicNotification(user, title, details, 
                NotificationType.SECURITY, Priority.HIGH);
            
            notification = notificationRepository.save(notification);
            sendNotificationAsync(notification);
            
            log.info("Security notification sent to user {} for event {}", userId, securityEvent);
            
        } catch (Exception e) {
            log.error("Failed to send security notification to user {}: {}", userId, e.getMessage());
        }
    }

    @Override
    public void sendWalletStatusNotification(String userId, String walletId, String status, String reason) {
        try {
            User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
            
            String title = "Wallet Status Changed";
            String message = String.format("Your wallet status has been changed to %s. Reason: %s", status, reason);
            
            Notification notification = createBasicNotification(user, title, message, 
                NotificationType.WALLET_STATUS, Priority.MEDIUM);
            notification.setRelatedWalletId(walletId);
            
            notification = notificationRepository.save(notification);
            sendNotificationAsync(notification);
            
            log.info("Wallet status notification sent to user {} for wallet {}", userId, walletId);
            
        } catch (Exception e) {
            log.error("Failed to send wallet status notification to user {}: {}", userId, e.getMessage());
        }
    }

    @Override
    public void sendBalanceAlertNotification(String userId, String walletId, double balance, String alertType) {
        try {
            User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
            
            String title = "Balance Alert";
            String message = String.format("Your wallet balance is %.2f. Alert type: %s", balance, alertType);
            
            Notification notification = createBasicNotification(user, title, message, 
                NotificationType.BALANCE_ALERT, Priority.MEDIUM);
            notification.setRelatedWalletId(walletId);
            
            notification = notificationRepository.save(notification);
            sendNotificationAsync(notification);
            
            log.info("Balance alert notification sent to user {} for wallet {}", userId, walletId);
            
        } catch (Exception e) {
            log.error("Failed to send balance alert notification to user {}: {}", userId, e.getMessage());
        }
    }

    @Override
    public void sendOtpCode(String userId, String purpose, String code) {
        try {
            User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
            
            List<String> availableChannels = getAvailableChannelsForUser(user);
            sendOtpCodeMultiChannel(userId, purpose, code, availableChannels);
            
            log.info("OTP code sent to user {} for purpose {} via channels {}", userId, purpose, availableChannels);
            
        } catch (Exception e) {
            log.error("Failed to send OTP code to user {}: {}", userId, e.getMessage());
        }
    }

    @Override
    public CustomApiResponse<Map<String, String>> sendOtpCodeMultiChannel(String userId, String purpose, String code, 
                                                                         List<String> channels) {
        try {
            User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
            
            Map<String, String> deliveryResults = new HashMap<>();
            String template = OTP_TEMPLATES.getOrDefault(purpose.toUpperCase(), OTP_TEMPLATES.get("DEFAULT"));
            String message = template.replace("{code}", code);
            
            // Create notification record for tracking
            Notification notification = createOtpNotification(user, purpose, message, channels);
            notification = notificationRepository.save(notification);
            
            // Send via each requested channel
            for (String channel : channels) {
                try {
                    boolean success = sendViaChannel(user, message, channel, notification.getRequestId());
                    deliveryResults.put(channel, success ? "SENT" : "FAILED");
                    
                    // Update delivery status
                    updateDeliveryStatus(notification.getRequestId(), channel, success, null);
                    
                } catch (Exception e) {
                    deliveryResults.put(channel, "FAILED");
                    updateDeliveryStatus(notification.getRequestId(), channel, false, e.getMessage());
                    log.error("Failed to send OTP via channel {} to user {}: {}", channel, userId, e.getMessage());
                }
            }
            
            // Update notification status based on delivery results
            boolean anySuccess = deliveryResults.values().stream().anyMatch("SENT"::equals);
            notification.setStatus(anySuccess ? Status.ACTIVE : Status.CANCELLED);
            notification.setIsDelivered(anySuccess);
            if (anySuccess) {
                notification.setSentAt(Instant.now());
                notification.setDeliveredAt(Instant.now());
            }
            notificationRepository.save(notification);
            
            return CustomApiResponse.success("OTP sent via multiple channels", deliveryResults);
            
        } catch (Exception e) {
            log.error("Failed to send OTP code via multiple channels to user {}: {}", userId, e.getMessage());
            return CustomApiResponse.error("Failed to send OTP: " + e.getMessage(), 
                UUID.randomUUID().toString(), List.of(e.getMessage()));
        }
    }

    @Override
    public CustomApiResponse<Map<String, Object>> getDeliveryStatus(String notificationId) {
        try {
            Notification notification = notificationRepository.findById(Long.valueOf(notificationId))
                .orElse(null);
            
            if (notification == null) {
                // Try to find by request ID
                notification = notificationRepository.findByRequestId(notificationId)
                    .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
            }
            
            DeliveryStatus status = deliveryStatusMap.get(notification.getRequestId());
            
            Map<String, Object> deliveryInfo = new HashMap<>();
            deliveryInfo.put("notificationId", notification.getId());
            deliveryInfo.put("requestId", notification.getRequestId());
            deliveryInfo.put("status", notification.getStatus().toString());
            deliveryInfo.put("isDelivered", notification.getIsDelivered());
            deliveryInfo.put("sentAt", notification.getSentAt());
            deliveryInfo.put("deliveredAt", notification.getDeliveredAt());
            deliveryInfo.put("retryCount", notification.getRetryCount());
            deliveryInfo.put("errorMessage", notification.getErrorMessage());
            
            if (status != null) {
                deliveryInfo.put("channelResults", status.getChannelResults());
                deliveryInfo.put("lastAttempt", status.getLastAttempt());
            }
            
            return CustomApiResponse.success("Delivery status retrieved successfully", deliveryInfo);
            
        } catch (Exception e) {
            log.error("Failed to get delivery status for notification {}: {}", notificationId, e.getMessage());
            return CustomApiResponse.error("Failed to retrieve delivery status: " + e.getMessage(), 
                UUID.randomUUID().toString(), List.of(e.getMessage()));
        }
    }

    @Override
    @Transactional
    public CustomApiResponse<String> retryNotificationDelivery(String notificationId) {
        try {
            Notification notification = notificationRepository.findById(Long.valueOf(notificationId))
                .orElse(null);
            
            if (notification == null) {
                notification = notificationRepository.findByRequestId(notificationId)
                    .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
            }
            
            if (notification.getRetryCount() >= maxRetryAttempts) {
                return CustomApiResponse.error("Maximum retry attempts exceeded", 
                    UUID.randomUUID().toString(), List.of("Maximum retry attempts exceeded"));
            }
            
            // Reset status for retry
            notification.setStatus(Status.PENDING);
            notification.setRetryCount(notification.getRetryCount() + 1);
            notification.setLastRetryAt(Instant.now());
            notification.setNextRetryAt(Instant.now().plusSeconds(retryDelaySeconds));
            notification.setErrorMessage(null);
            notification.setErrorCode(null);
            
            notification = notificationRepository.save(notification);
            
            // Retry delivery
            sendNotificationAsync(notification);
            
            return CustomApiResponse.success("Notification retry initiated", "Success");
            
        } catch (Exception e) {
            log.error("Failed to retry notification delivery {}: {}", notificationId, e.getMessage());
            return CustomApiResponse.error("Failed to retry notification delivery: " + e.getMessage(), 
                UUID.randomUUID().toString(), List.of(e.getMessage()));
        }
    }

    @Override
    public CustomApiResponse<List<String>> getAvailableChannels(String userId) {
        try {
            User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
            
            List<String> channels = getAvailableChannelsForUser(user);
            
            return CustomApiResponse.success("Available channels retrieved successfully", channels);
            
        } catch (Exception e) {
            log.error("Failed to get available channels for user {}: {}", userId, e.getMessage());
            return CustomApiResponse.error("Failed to retrieve available channels: " + e.getMessage(), 
                UUID.randomUUID().toString(), List.of(e.getMessage()));
        }
    }

    // Private helper methods

    private Notification createNotification(User user, NotificationRequest request) {
        Notification notification = new Notification();
        notification.setRequestId(RequestIdGenerator.generateRequestId());
        notification.setUser(user);
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setStatus(Status.PENDING);
        notification.setType(NotificationType.valueOf(request.getType()));
        notification.setPriority(Priority.valueOf(request.getPriority()));
        notification.setChannel(request.getChannel());
        notification.setLanguage(user.getLocale());
        notification.setCreatedBy("SYSTEM");
        return notification;
    }

    private Notification createTemplatedNotification(User user, NotificationType type, Priority priority, 
                                                   String templateId, Map<String, Object> templateData) {
        Notification notification = new Notification();
        notification.setRequestId(RequestIdGenerator.generateRequestId());
        notification.setUser(user);
        notification.setTitle("Templated Notification");
        notification.setMessage("Message from template: " + templateId);
        notification.setStatus(Status.PENDING);
        notification.setType(type);
        notification.setPriority(priority);
        notification.setTemplateId(templateId);
        notification.setTemplateData(templateData.toString());
        notification.setLanguage(user.getLocale());
        notification.setCreatedBy("SYSTEM");
        return notification;
    }

    private Notification createBasicNotification(User user, String title, String message, 
                                               NotificationType type, Priority priority) {
        Notification notification = new Notification();
        notification.setRequestId(RequestIdGenerator.generateRequestId());
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setStatus(Status.PENDING);
        notification.setType(type);
        notification.setPriority(priority);
        notification.setLanguage(user.getLocale());
        notification.setCreatedBy("SYSTEM");
        return notification;
    }

    private Notification createOtpNotification(User user, String purpose, String message, List<String> channels) {
        Notification notification = new Notification();
        notification.setRequestId(RequestIdGenerator.generateRequestId());
        notification.setUser(user);
        notification.setTitle("OTP Verification");
        notification.setMessage(message);
        notification.setStatus(Status.PENDING);
        notification.setType(NotificationType.SECURITY);
        notification.setPriority(Priority.HIGH);
        notification.setChannel(String.join(",", channels));
        notification.setTemplateId("OTP_" + purpose.toUpperCase());
        notification.setLanguage(user.getLocale());
        notification.setCreatedBy("SYSTEM");
        notification.setRequiresAcknowledgment(false);
        notification.setMaxRetries(maxRetryAttempts);
        return notification;
    }

    private void sendNotificationAsync(Notification notification) {
        CompletableFuture.runAsync(() -> {
            try {
                List<String> channels = Arrays.asList(notification.getChannel().split(","));
                boolean success = false;
                
                for (String channel : channels) {
                    try {
                        boolean channelSuccess = sendViaChannel(notification.getUser(), 
                            notification.getMessage(), channel.trim(), notification.getRequestId());
                        if (channelSuccess) {
                            success = true;
                        }
                    } catch (Exception e) {
                        log.error("Failed to send notification via channel {}: {}", channel, e.getMessage());
                    }
                }
                
                // Update notification status
                notification.setStatus(success ? Status.ACTIVE : Status.CANCELLED);
                notification.setIsDelivered(success);
                if (success) {
                    notification.setSentAt(Instant.now());
                    notification.setDeliveredAt(Instant.now());
                }
                notificationRepository.save(notification);
                
            } catch (Exception e) {
                log.error("Failed to send notification {}: {}", notification.getRequestId(), e.getMessage());
                notification.setStatus(Status.CANCELLED);
                notification.setErrorMessage(e.getMessage());
                notificationRepository.save(notification);
            }
        }, notificationExecutor);
    }

    private boolean sendViaChannel(User user, String message, String channel, String requestId) {
        switch (channel.toUpperCase()) {
            case "SMS":
                return sendSms(user, message, requestId);
            case "EMAIL":
                return sendEmail(user, message, requestId);
            case "PUSH":
                return sendPushNotification(user, message, requestId);
            case "IN_APP":
                return true; // In-app notifications are already stored in database
            default:
                log.warn("Unknown notification channel: {}", channel);
                return false;
        }
    }

    private boolean sendSms(User user, String message, String requestId) {
        if (!smsEnabled) {
            log.warn("SMS notifications are disabled");
            return false;
        }
        
        try {
            // Simulate SMS sending (integrate with actual SMS provider like Twilio, AWS SNS, etc.)
            String phoneNumber = user.getCountryCode() + user.getMobile();
            log.info("Sending SMS to {}: {}", phoneNumber, message);
            
            // Simulate network delay
            Thread.sleep(100);
            
            // Simulate 95% success rate
            boolean success = Math.random() < 0.95;
            
            if (success) {
                log.info("SMS sent successfully to {} for request {}", phoneNumber, requestId);
            } else {
                log.warn("SMS delivery failed to {} for request {}", phoneNumber, requestId);
            }
            
            return success;
            
        } catch (Exception e) {
            log.error("Failed to send SMS to user {}: {}", user.getId(), e.getMessage());
            return false;
        }
    }

    private boolean sendEmail(User user, String message, String requestId) {
        if (!emailEnabled) {
            log.warn("Email notifications are disabled");
            return false;
        }
        
        try {
            // Use real email service for OTP and notifications
            String email = user.getEmail();
            if (email == null || email.trim().isEmpty()) {
                log.warn("User {} has no email address", user.getId());
                return false;
            }
            
            log.info("Sending email to {}: {}", email, message);
            
            // Determine if this is an OTP email by checking the message content
            boolean isOtpEmail = message.contains("OTP") || message.contains("verification code");
            String purpose = "DEFAULT";
            
            if (isOtpEmail) {
                // Extract purpose from message or use default
                for (String key : OTP_TEMPLATES.keySet()) {
                    if (message.contains(key.toLowerCase()) || message.contains(key.replace("_", " ").toLowerCase())) {
                        purpose = key;
                        break;
                    }
                }
                
                // Extract OTP code from the message
                String otpCode = extractOtpCode(message);
                if (otpCode.equals("000000")) {
                    log.warn("Could not extract valid OTP code from message for user {}", user.getId());
                    // Fallback: send as regular notification
                    String subject = "Digital Wallet OTP";
                    return emailService.sendNotificationEmail(user, subject, message, requestId);
                }
                
                // Send OTP email
                return emailService.sendOtpEmail(user, otpCode, purpose, requestId);
            } else {
                // Send regular notification email
                String subject = "Digital Wallet Notification";
                return emailService.sendNotificationEmail(user, subject, message, requestId);
            }
            
        } catch (Exception e) {
            log.error("Failed to send email to user {}: {}", user.getId(), e.getMessage());
            return false;
        }
    }
    
    private String extractOtpCode(String message) {
        // Extract OTP code from message (assuming format: "Your OTP is: 123456")
        try {
            if (message.contains("is:")) {
                String[] parts = message.split("is:");
                if (parts.length > 1) {
                    String codePart = parts[1].trim();
                    // Extract the first 6-digit number
                    String[] words = codePart.split("\\s+");
                    for (String word : words) {
                        if (word.matches("\\d{6}")) {
                            return word;
                        }
                    }
                }
            }
            // Fallback: try to find any 6-digit number
            String[] words = message.split("\\s+");
            for (String word : words) {
                if (word.matches("\\d{6}")) {
                    return word;
                }
            }
            return "000000"; // Fallback code
        } catch (Exception e) {
            log.warn("Failed to extract OTP code from message: {}", e.getMessage());
            return "000000";
        }
    }

    private boolean sendPushNotification(User user, String message, String requestId) {
        if (!pushEnabled) {
            log.warn("Push notifications are disabled");
            return false;
        }
        
        try {
            // Simulate push notification sending (integrate with FCM, APNS, etc.)
            log.info("Sending push notification to user {}: {}", user.getId(), message);
            
            // Simulate network delay
            Thread.sleep(50);
            
            // Simulate 90% success rate (push notifications can fail if app is uninstalled)
            boolean success = Math.random() < 0.90;
            
            if (success) {
                log.info("Push notification sent successfully to user {} for request {}", user.getId(), requestId);
            } else {
                log.warn("Push notification delivery failed to user {} for request {}", user.getId(), requestId);
            }
            
            return success;
            
        } catch (Exception e) {
            log.error("Failed to send push notification to user {}: {}", user.getId(), e.getMessage());
            return false;
        }
    }

    private List<String> getAvailableChannelsForUser(User user) {
        List<String> channels = new ArrayList<>();
        
        // SMS is available if user has mobile number
        if (smsEnabled && user.getMobile() != null && !user.getMobile().trim().isEmpty()) {
            channels.add("SMS");
        }
        
        // Email is available if user has email address
        if (emailEnabled && user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            channels.add("EMAIL");
        }
        
        // Push notifications are always available (assuming app is installed)
        if (pushEnabled) {
            channels.add("PUSH");
        }
        
        // In-app notifications are always available
        channels.add("IN_APP");
        
        return channels;
    }

    private void updateDeliveryStatus(String requestId, String channel, boolean success, String errorMessage) {
        DeliveryStatus status = deliveryStatusMap.computeIfAbsent(requestId, k -> new DeliveryStatus());
        status.updateChannelResult(channel, success, errorMessage);
        status.setLastAttempt(Instant.now());
    }

    private NotificationResponse mapToResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setRequestId(notification.getRequestId());
        response.setUserId(notification.getUser().getId().toString());
        response.setTitle(notification.getTitle());
        response.setMessage(notification.getMessage());
        response.setType(notification.getType().toString());
        response.setStatus(notification.getStatus().toString());
        response.setPriority(notification.getPriority().toString());
        response.setChannel(notification.getChannel());
        response.setRelatedTransactionId(notification.getRelatedTransactionId());
        response.setRelatedWalletId(notification.getRelatedWalletId());
        response.setTemplateId(notification.getTemplateId());
        response.setIsRead(notification.getIsRead());
        response.setIsDelivered(notification.getIsDelivered());
        response.setRequiresAcknowledgment(notification.getRequiresAcknowledgment());
        response.setSentAt(notification.getSentAt());
        response.setDeliveredAt(notification.getDeliveredAt());
        response.setReadAt(notification.getReadAt());
        response.setAcknowledgedAt(notification.getAcknowledgedAt());
        response.setCreatedAt(notification.getCreatedAt());
        response.setUpdatedAt(notification.getUpdatedAt());
        return response;
    }

    // Inner class for tracking delivery status
    private static class DeliveryStatus {
        private final Map<String, ChannelResult> channelResults = new HashMap<>();
        private Instant lastAttempt;

        public void updateChannelResult(String channel, boolean success, String errorMessage) {
            channelResults.put(channel, new ChannelResult(success, errorMessage, Instant.now()));
        }

        public Map<String, ChannelResult> getChannelResults() {
            return channelResults;
        }

        public Instant getLastAttempt() {
            return lastAttempt;
        }

        public void setLastAttempt(Instant lastAttempt) {
            this.lastAttempt = lastAttempt;
        }
    }

    private static class ChannelResult {
        private final boolean success;
        private final String errorMessage;
        private final Instant timestamp;

        public ChannelResult(boolean success, String errorMessage, Instant timestamp) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.timestamp = timestamp;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public Instant getTimestamp() {
            return timestamp;
        }
    }
}