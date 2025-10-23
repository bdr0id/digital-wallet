package com.boit_droid.wallet.service;

import com.boit_droid.wallet.entity.User;

public interface EmailService {
    
    /**
     * Send OTP email to user
     * @param user The user to send email to
     * @param otpCode The OTP code to send
     * @param purpose The purpose of the OTP (e.g., KYC, TRANSFER, etc.)
     * @param requestId The request ID for tracking
     * @return true if email sent successfully, false otherwise
     */
    boolean sendOtpEmail(User user, String otpCode, String purpose, String requestId);
    
    /**
     * Send general notification email to user
     * @param user The user to send email to
     * @param subject The email subject
     * @param message The email message
     * @param requestId The request ID for tracking
     * @return true if email sent successfully, false otherwise
     */
    boolean sendNotificationEmail(User user, String subject, String message, String requestId);
    
    /**
     * Test email service connectivity
     * @return true if email service is working, false otherwise
     */
    boolean testEmailService();
}

