package com.boit_droid.wallet.service.impl;

import com.boit_droid.wallet.repository.AuditTrailRepository;
import com.boit_droid.wallet.service.SecurityMonitoringService;
import com.boit_droid.wallet.service.OtpAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityMonitoringServiceImpl implements SecurityMonitoringService {

    private final AuditTrailRepository auditTrailRepository;
    private final OtpAuditService otpAuditService;

    // In-memory cache for real-time pattern detection
    private final Map<String, List<SecurityEvent>> securityEventCache = new ConcurrentHashMap<>();
    
    // Thresholds for pattern detection
    private static final int BRUTE_FORCE_THRESHOLD = 5; // 5 failed attempts
    private static final int DISTRIBUTED_ATTACK_THRESHOLD = 3; // 3 different IPs
    private static final int ENUMERATION_THRESHOLD = 10; // 10 different users from same IP
    private static final long DEFAULT_TIME_WINDOW = 300; // 5 minutes

    @Override
    public SecurityAssessment analyzeOtpPatterns(String userId, String clientIp, long timeWindow) {
        cleanExpiredEvents();
        
        List<String> detectedPatterns = new ArrayList<>();
        String riskLevel = "LOW";
        String reason = "Normal activity pattern";
        boolean actionRequired = false;
        String recommendedAction = "Continue monitoring";

        // Check for brute force patterns
        if (detectBruteForcePattern(userId, clientIp, timeWindow)) {
            detectedPatterns.add("BRUTE_FORCE_ATTACK");
            riskLevel = "HIGH";
            reason = "Multiple failed OTP attempts detected";
            actionRequired = true;
            recommendedAction = "Temporarily block user account";
        }

        // Check for distributed attack patterns
        if (detectDistributedAttackPattern(userId, timeWindow)) {
            detectedPatterns.add("DISTRIBUTED_ATTACK");
            riskLevel = "CRITICAL";
            reason = "Attack from multiple IP addresses detected";
            actionRequired = true;
            recommendedAction = "Block user account and investigate";
        }

        // Check for account enumeration
        if (detectAccountEnumerationPattern(clientIp, timeWindow)) {
            detectedPatterns.add("ACCOUNT_ENUMERATION");
            if ("LOW".equals(riskLevel)) {
                riskLevel = "MEDIUM";
                reason = "Possible account enumeration attempt";
                actionRequired = true;
                recommendedAction = "Monitor IP address closely";
            }
        }

        // Check for rapid successive requests
        if (detectRapidRequestPattern(userId, clientIp, timeWindow)) {
            detectedPatterns.add("RAPID_REQUESTS");
            if ("LOW".equals(riskLevel)) {
                riskLevel = "MEDIUM";
                reason = "Unusually rapid request pattern detected";
                actionRequired = true;
                recommendedAction = "Apply additional rate limiting";
            }
        }

        return new SecurityAssessment(riskLevel, reason, detectedPatterns, actionRequired, recommendedAction);
    }

    @Override
    public boolean detectBruteForcePattern(String userId, String clientIp, long timeWindow) {
        String key = userId + ":" + clientIp;
        List<SecurityEvent> events = securityEventCache.get(key);
        
        if (events == null) {
            return false;
        }

        Instant cutoff = Instant.now().minusSeconds(timeWindow);
        long failedAttempts = events.stream()
            .filter(event -> event.getTimestamp().isAfter(cutoff))
            .filter(event -> !event.isSuccessful())
            .filter(event -> "OTP_VERIFICATION".equals(event.getEventType()))
            .count();

        return failedAttempts >= BRUTE_FORCE_THRESHOLD;
    }

    @Override
    public boolean detectDistributedAttackPattern(String userId, long timeWindow) {
        Instant cutoff = Instant.now().minusSeconds(timeWindow);
        
        Set<String> uniqueIps = new HashSet<>();
        for (Map.Entry<String, List<SecurityEvent>> entry : securityEventCache.entrySet()) {
            if (entry.getKey().startsWith(userId + ":")) {
                String ip = entry.getKey().substring(entry.getKey().indexOf(':') + 1);
                
                boolean hasRecentFailures = entry.getValue().stream()
                    .anyMatch(event -> event.getTimestamp().isAfter(cutoff) && 
                             !event.isSuccessful() && 
                             "OTP_VERIFICATION".equals(event.getEventType()));
                
                if (hasRecentFailures) {
                    uniqueIps.add(ip);
                }
            }
        }

        return uniqueIps.size() >= DISTRIBUTED_ATTACK_THRESHOLD;
    }

    @Override
    public boolean detectAccountEnumerationPattern(String clientIp, long timeWindow) {
        Instant cutoff = Instant.now().minusSeconds(timeWindow);
        
        Set<String> uniqueUsers = new HashSet<>();
        for (Map.Entry<String, List<SecurityEvent>> entry : securityEventCache.entrySet()) {
            if (entry.getKey().endsWith(":" + clientIp)) {
                String userId = entry.getKey().substring(0, entry.getKey().indexOf(':'));
                
                boolean hasRecentActivity = entry.getValue().stream()
                    .anyMatch(event -> event.getTimestamp().isAfter(cutoff));
                
                if (hasRecentActivity) {
                    uniqueUsers.add(userId);
                }
            }
        }

        return uniqueUsers.size() >= ENUMERATION_THRESHOLD;
    }

    @Override
    public List<SecurityRecommendation> getSecurityRecommendations(String userId, String clientIp) {
        List<SecurityRecommendation> recommendations = new ArrayList<>();
        
        SecurityAssessment assessment = analyzeOtpPatterns(userId, clientIp, DEFAULT_TIME_WINDOW);
        
        if ("HIGH".equals(assessment.getRiskLevel()) || "CRITICAL".equals(assessment.getRiskLevel())) {
            recommendations.add(new SecurityRecommendation(
                "ACCOUNT_SECURITY",
                "Temporarily restrict account access due to suspicious activity",
                "HIGH",
                "TEMPORARY_ACCOUNT_LOCK"
            ));
        }

        if (assessment.getDetectedPatterns().contains("BRUTE_FORCE_ATTACK")) {
            recommendations.add(new SecurityRecommendation(
                "AUTHENTICATION",
                "Implement additional authentication factors",
                "HIGH",
                "ENABLE_2FA"
            ));
        }

        if (assessment.getDetectedPatterns().contains("DISTRIBUTED_ATTACK")) {
            recommendations.add(new SecurityRecommendation(
                "NETWORK_SECURITY",
                "Block suspicious IP addresses",
                "CRITICAL",
                "IP_BLACKLIST"
            ));
        }

        if (assessment.getDetectedPatterns().contains("ACCOUNT_ENUMERATION")) {
            recommendations.add(new SecurityRecommendation(
                "RATE_LIMITING",
                "Apply stricter rate limiting for this IP",
                "MEDIUM",
                "ENHANCED_RATE_LIMIT"
            ));
        }

        return recommendations;
    }

    @Override
    public void recordSecurityEvent(String userId, String clientIp, String eventType, boolean successful, Instant timestamp) {
        String key = userId + ":" + clientIp;
        SecurityEvent event = new SecurityEvent(eventType, successful, timestamp);
        
        securityEventCache.computeIfAbsent(key, k -> new ArrayList<>()).add(event);
        
        // Log to audit service if it's a security-relevant event
        if (!successful && ("OTP_VERIFICATION".equals(eventType) || "OTP_GENERATION".equals(eventType))) {
            otpAuditService.logSecurityEvent(userId, clientIp, "REQUEST_ID_NOT_AVAILABLE", 
                eventType, "Security event recorded: " + eventType, 
                successful ? "LOW" : "MEDIUM");
        }
    }

    private boolean detectRapidRequestPattern(String userId, String clientIp, long timeWindow) {
        String key = userId + ":" + clientIp;
        List<SecurityEvent> events = securityEventCache.get(key);
        
        if (events == null || events.size() < 3) {
            return false;
        }

        Instant cutoff = Instant.now().minusSeconds(timeWindow);
        List<SecurityEvent> recentEvents = events.stream()
            .filter(event -> event.getTimestamp().isAfter(cutoff))
            .sorted(Comparator.comparing(SecurityEvent::getTimestamp))
            .toList();

        if (recentEvents.size() < 3) {
            return false;
        }

        // Check if there are more than 3 requests within 30 seconds
        for (int i = 0; i <= recentEvents.size() - 3; i++) {
            Instant firstRequest = recentEvents.get(i).getTimestamp();
            Instant thirdRequest = recentEvents.get(i + 2).getTimestamp();
            
            if (thirdRequest.minusSeconds(30).isBefore(firstRequest)) {
                return true;
            }
        }

        return false;
    }

    private void cleanExpiredEvents() {
        Instant cutoff = Instant.now().minusSeconds(3600); // Keep events for 1 hour
        
        securityEventCache.entrySet().removeIf(entry -> {
            entry.getValue().removeIf(event -> event.getTimestamp().isBefore(cutoff));
            return entry.getValue().isEmpty();
        });
    }

    private static class SecurityEvent {
        private final String eventType;
        private final boolean successful;
        private final Instant timestamp;

        public SecurityEvent(String eventType, boolean successful, Instant timestamp) {
            this.eventType = eventType;
            this.successful = successful;
            this.timestamp = timestamp;
        }

        public String getEventType() { return eventType; }
        public boolean isSuccessful() { return successful; }
        public Instant getTimestamp() { return timestamp; }
    }
}