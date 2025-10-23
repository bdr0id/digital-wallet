package com.boit_droid.wallet.service;

import com.boit_droid.wallet.entity.audit.AuditTrail;

/**
 * Service for auditing OTP operations and security events
 */
public interface OtpAuditService {

    /**
     * Log OTP generation event
     * @param userId The user ID requesting the OTP
     * @param purpose The purpose of the OTP request
     * @param clientIp The client IP address
     * @param requestId The request ID for correlation
     * @param successful Whether the OTP generation was successful
     * @param errorMessage Error message if generation failed
     */
    void logOtpGeneration(String userId, String purpose, String clientIp, String requestId, 
                         boolean successful, String errorMessage);

    /**
     * Log OTP verification event
     * @param userId The user ID verifying the OTP
     * @param purpose The purpose of the OTP verification
     * @param clientIp The client IP address
     * @param requestId The request ID for correlation
     * @param successful Whether the OTP verification was successful
     * @param failureReason Reason for verification failure
     * @param attemptNumber The attempt number for this OTP
     */
    void logOtpVerification(String userId, String purpose, String clientIp, String requestId,
                           boolean successful, String failureReason, int attemptNumber);

    /**
     * Log rate limiting event
     * @param userId The user ID that was rate limited
     * @param clientIp The client IP address
     * @param requestId The request ID for correlation
     * @param rateLimitType The type of rate limit (USER, IP, GLOBAL)
     * @param currentCount Current request count
     * @param limit The rate limit threshold
     */
    void logRateLimitExceeded(String userId, String clientIp, String requestId, 
                             String rateLimitType, int currentCount, int limit);

    /**
     * Log suspicious activity detection
     * @param userId The user ID involved in suspicious activity
     * @param clientIp The client IP address
     * @param requestId The request ID for correlation
     * @param suspiciousPattern The pattern that was detected
     * @param riskLevel The assessed risk level
     * @param actionTaken The action taken in response
     */
    void logSuspiciousActivity(String userId, String clientIp, String requestId,
                              String suspiciousPattern, String riskLevel, String actionTaken);

    /**
     * Log security event
     * @param userId The user ID involved
     * @param clientIp The client IP address
     * @param requestId The request ID for correlation
     * @param eventType The type of security event
     * @param description Description of the event
     * @param riskLevel The assessed risk level
     */
    void logSecurityEvent(String userId, String clientIp, String requestId,
                         String eventType, String description, String riskLevel);

    /**
     * Create base audit trail for OTP operations
     * @param userId The user ID
     * @param operation The operation type
     * @param clientIp The client IP address
     * @param requestId The request ID
     * @return Base AuditTrail object
     */
    AuditTrail createBaseAuditTrail(String userId, String operation, String clientIp, String requestId);
}