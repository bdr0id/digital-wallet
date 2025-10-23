package com.boit_droid.wallet.service;

import com.boit_droid.wallet.dto.request.NotificationRequest;
import com.boit_droid.wallet.dto.response.CustomApiResponse;
import com.boit_droid.wallet.dto.response.NotificationResponse;
import com.boit_droid.wallet.dto.response.PagedResponse;
import com.boit_droid.wallet.entity.enums.NotificationType;
import com.boit_droid.wallet.entity.enums.Priority;

import java.util.List;
import java.util.Map;

public interface NotificationService {
    
    /**
     * Send a notification to a user
     */
    CustomApiResponse<NotificationResponse> sendNotification(String userId, NotificationRequest request);
    
    /**
     * Send a notification using a template
     */
    CustomApiResponse<NotificationResponse> sendTemplatedNotification(String userId, NotificationType type,
                                                                      Priority priority, String templateId,
                                                                      Map<String, Object> templateData);
    
    /**
     * Get notifications for a user with pagination
     */
    CustomApiResponse<PagedResponse<NotificationResponse>> getNotifications(String userId, int page, int size);
    
    /**
     * Get unread notifications for a user
     */
    CustomApiResponse<PagedResponse<NotificationResponse>> getUnreadNotifications(String userId, int page, int size);
    
    /**
     * Mark a notification as read
     */
    CustomApiResponse<String> markNotificationAsRead(String notificationId);
    
    /**
     * Mark all notifications as read for a user
     */
    CustomApiResponse<String> markAllNotificationsAsRead(String userId);
    
    /**
     * Get notification count for a user
     */
    CustomApiResponse<Map<String, Long>> getNotificationCounts(String userId);
    
    /**
     * Delete a notification
     */
    CustomApiResponse<String> deleteNotification(String notificationId);
    
    /**
     * Send transaction notification
     */
    void sendTransactionNotification(String userId, String transactionId, String walletId, 
                                   String transactionType, double amount, String currency);
    
    /**
     * Send KYC notification
     */
    void sendKYCNotification(String userId, String kycStatus, String message);
    
    /**
     * Send security notification
     */
    void sendSecurityNotification(String userId, String securityEvent, String details);
    
    /**
     * Send wallet status notification
     */
    void sendWalletStatusNotification(String userId, String walletId, String status, String reason);
    
    /**
     * Send balance alert notification
     */
    void sendBalanceAlertNotification(String userId, String walletId, double balance, String alertType);

	/**
	 * Send OTP code for a security-sensitive operation
	 */
	void sendOtpCode(String userId, String purpose, String code);
	
	/**
	 * Send OTP code through multiple channels with delivery tracking
	 */
	CustomApiResponse<Map<String, String>> sendOtpCodeMultiChannel(String userId, String purpose, String code, 
	                                                               List<String> channels);
	
	/**
	 * Get delivery status for a notification
	 */
	CustomApiResponse<Map<String, Object>> getDeliveryStatus(String notificationId);
	
	/**
	 * Retry failed notification delivery
	 */
	CustomApiResponse<String> retryNotificationDelivery(String notificationId);
	
	/**
	 * Get available notification channels for a user
	 */
	CustomApiResponse<List<String>> getAvailableChannels(String userId);
}
