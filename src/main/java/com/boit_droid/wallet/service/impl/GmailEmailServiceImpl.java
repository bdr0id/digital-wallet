package com.boit_droid.wallet.service.impl;

import com.boit_droid.wallet.entity.User;
import com.boit_droid.wallet.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GmailEmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${spring.mail.properties.mail.smtp.auth:true}")
    private boolean smtpAuthEnabled;
    
    @Value("${spring.mail.properties.mail.smtp.starttls.enable:true}")
    private boolean startTlsEnabled;
    
    // OTP email templates
    private static final Map<String, String> OTP_SUBJECTS = Map.of(
        "TRANSFER", "Your Wallet Transfer OTP",
        "PIN_UPDATE", "Your PIN Update OTP",
        "WALLET_STATUS", "Your Wallet Status Change OTP",
        "WALLET_DELETION", "Your Wallet Deletion OTP",
        "KYC", "Your KYC Verification OTP",
        "USER_STATUS", "Your Account Status Change OTP",
        "DEFAULT", "Your Verification OTP"
    );
    
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
    public boolean sendOtpEmail(User user, String otpCode, String purpose, String requestId) {
        try {
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                log.warn("User {} has no email address for OTP delivery", user.getId());
                return false;
            }
            
            String subject = OTP_SUBJECTS.getOrDefault(purpose.toUpperCase(), OTP_SUBJECTS.get("DEFAULT"));
            String template = OTP_TEMPLATES.getOrDefault(purpose.toUpperCase(), OTP_TEMPLATES.get("DEFAULT"));
            String message = template.replace("{code}", otpCode);
            
            // Add security notice
            message += "\n\nSecurity Notice: This OTP is valid for 5 minutes only. Never share this code with anyone.";
            message += "\nIf you didn't request this code, please contact support immediately.";
            
            return sendEmail(user.getEmail(), subject, message, requestId);
            
        } catch (Exception e) {
            log.error("Failed to send OTP email to user {}: {}", user.getId(), e.getMessage());
            return false;
        }
    }

    @Override
    public boolean sendNotificationEmail(User user, String subject, String message, String requestId) {
        try {
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                log.warn("User {} has no email address for notification delivery", user.getId());
                return false;
            }
            
            return sendEmail(user.getEmail(), subject, message, requestId);
            
        } catch (Exception e) {
            log.error("Failed to send notification email to user {}: {}", user.getId(), e.getMessage());
            return false;
        }
    }

    @Override
    public boolean testEmailService() {
        try {
            // Try to send a test email to verify SMTP configuration
            SimpleMailMessage testMessage = new SimpleMailMessage();
            testMessage.setFrom(fromEmail);
            testMessage.setTo(fromEmail); // Send to self for testing
            testMessage.setSubject("Digital Wallet - Email Service Test");
            testMessage.setText("This is a test email to verify the email service is working correctly.");
            
            mailSender.send(testMessage);
            log.info("Email service test successful - SMTP configuration is working");
            return true;
            
        } catch (Exception e) {
            log.error("Email service test failed: {}", e.getMessage());
            return false;
        }
    }
    
    private boolean sendEmail(String toEmail, String subject, String message, String requestId) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(fromEmail);
            mailMessage.setTo(toEmail);
            mailMessage.setSubject(subject);
            mailMessage.setText(message);
            
            mailSender.send(mailMessage);
            
            log.info("Email sent successfully to {} for request {}: {}", toEmail, requestId, subject);
            return true;
            
        } catch (Exception e) {
            log.error("Failed to send email to {} for request {}: {}", toEmail, requestId, e.getMessage());
            return false;
        }
    }
}
