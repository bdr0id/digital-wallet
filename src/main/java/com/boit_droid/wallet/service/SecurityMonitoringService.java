package com.boit_droid.wallet.service;

import java.time.Instant;
import java.util.List;

/**
 * Service for monitoring security events and detecting suspicious patterns
 */
public interface SecurityMonitoringService {

    /**
     * Analyze OTP patterns for suspicious activity
     * @param userId The user ID to analyze
     * @param clientIp The client IP address
     * @param timeWindow The time window to analyze (in seconds)
     * @return Security assessment result
     */
    SecurityAssessment analyzeOtpPatterns(String userId, String clientIp, long timeWindow);

    /**
     * Check for brute force attack patterns
     * @param userId The user ID to check
     * @param clientIp The client IP address
     * @param timeWindow The time window to check (in seconds)
     * @return true if brute force pattern detected
     */
    boolean detectBruteForcePattern(String userId, String clientIp, long timeWindow);

    /**
     * Check for distributed attack patterns across multiple IPs
     * @param userId The user ID to check
     * @param timeWindow The time window to check (in seconds)
     * @return true if distributed attack pattern detected
     */
    boolean detectDistributedAttackPattern(String userId, long timeWindow);

    /**
     * Check for account enumeration attempts
     * @param clientIp The client IP address
     * @param timeWindow The time window to check (in seconds)
     * @return true if enumeration pattern detected
     */
    boolean detectAccountEnumerationPattern(String clientIp, long timeWindow);

    /**
     * Get security recommendations based on current threat level
     * @param userId The user ID
     * @param clientIp The client IP address
     * @return List of security recommendations
     */
    List<SecurityRecommendation> getSecurityRecommendations(String userId, String clientIp);

    /**
     * Record security event for pattern analysis
     * @param userId The user ID
     * @param clientIp The client IP address
     * @param eventType The type of security event
     * @param successful Whether the event was successful
     * @param timestamp The timestamp of the event
     */
    void recordSecurityEvent(String userId, String clientIp, String eventType, boolean successful, Instant timestamp);

    /**
     * Security assessment result
     */
    class SecurityAssessment {
        private final String riskLevel;
        private final String reason;
        private final List<String> detectedPatterns;
        private final boolean actionRequired;
        private final String recommendedAction;

        public SecurityAssessment(String riskLevel, String reason, List<String> detectedPatterns, 
                                boolean actionRequired, String recommendedAction) {
            this.riskLevel = riskLevel;
            this.reason = reason;
            this.detectedPatterns = detectedPatterns;
            this.actionRequired = actionRequired;
            this.recommendedAction = recommendedAction;
        }

        public String getRiskLevel() { return riskLevel; }
        public String getReason() { return reason; }
        public List<String> getDetectedPatterns() { return detectedPatterns; }
        public boolean isActionRequired() { return actionRequired; }
        public String getRecommendedAction() { return recommendedAction; }
    }

    /**
     * Security recommendation
     */
    class SecurityRecommendation {
        private final String type;
        private final String description;
        private final String priority;
        private final String action;

        public SecurityRecommendation(String type, String description, String priority, String action) {
            this.type = type;
            this.description = description;
            this.priority = priority;
            this.action = action;
        }

        public String getType() { return type; }
        public String getDescription() { return description; }
        public String getPriority() { return priority; }
        public String getAction() { return action; }
    }
}