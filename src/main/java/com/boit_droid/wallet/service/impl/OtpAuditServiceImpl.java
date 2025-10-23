package com.boit_droid.wallet.service.impl;

import com.boit_droid.wallet.entity.audit.AuditTrail;
import com.boit_droid.wallet.repository.AuditTrailRepository;
import com.boit_droid.wallet.service.OtpAuditService;
import com.boit_droid.wallet.util.RequestIdGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpAuditServiceImpl implements OtpAuditService {

    private final AuditTrailRepository auditTrailRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void logOtpGeneration(String userId, String purpose, String clientIp, String requestId, 
                                boolean successful, String errorMessage) {
        try {
            AuditTrail audit = createBaseAuditTrail(userId, "OTP_GENERATION", clientIp, requestId);
            
            audit.setEntityType("OTP");
            audit.setEntityId(userId + ":" + purpose);
            audit.setChangeDescription("OTP generation for purpose: " + purpose);
            audit.setIsSensitiveOperation(true);
            audit.setIsSuccessful(successful);
            audit.setRiskLevel(determineRiskLevel(purpose));
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("purpose", purpose);
            metadata.put("successful", successful);
            if (errorMessage != null) {
                metadata.put("errorMessage", errorMessage);
                audit.setErrorMessage(errorMessage);
            }
            
            audit.setAdditionalMetadata(objectMapper.writeValueAsString(metadata));
            audit.setTags("otp,generation,security");
            
            if (!successful) {
                audit.setAlertGenerated(true);
                audit.setAlertLevel("WARNING");
                audit.setAlertMessage("OTP generation failed for user: " + userId);
            }
            
            auditTrailRepository.save(audit);
            log.debug("OTP generation audit logged for user: {}, purpose: {}, successful: {}", 
                     userId, purpose, successful);
                     
        } catch (Exception e) {
            log.error("Failed to log OTP generation audit for user: {}", userId, e);
        }
    }

    @Override
    public void logOtpVerification(String userId, String purpose, String clientIp, String requestId,
                                  boolean successful, String failureReason, int attemptNumber) {
        try {
            AuditTrail audit = createBaseAuditTrail(userId, "OTP_VERIFICATION", clientIp, requestId);
            
            audit.setEntityType("OTP");
            audit.setEntityId(userId + ":" + purpose);
            audit.setChangeDescription("OTP verification for purpose: " + purpose + ", attempt: " + attemptNumber);
            audit.setIsSensitiveOperation(true);
            audit.setIsSuccessful(successful);
            audit.setRiskLevel(determineVerificationRiskLevel(successful, attemptNumber));
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("purpose", purpose);
            metadata.put("successful", successful);
            metadata.put("attemptNumber", attemptNumber);
            if (failureReason != null) {
                metadata.put("failureReason", failureReason);
                audit.setErrorMessage(failureReason);
            }
            
            audit.setAdditionalMetadata(objectMapper.writeValueAsString(metadata));
            audit.setTags("otp,verification,security");
            
            // Generate alerts for failed attempts
            if (!successful) {
                audit.setAlertGenerated(true);
                if (attemptNumber >= 3) {
                    audit.setAlertLevel("CRITICAL");
                    audit.setAlertMessage("Multiple failed OTP attempts detected for user: " + userId);
                } else {
                    audit.setAlertLevel("WARNING");
                    audit.setAlertMessage("OTP verification failed for user: " + userId);
                }
            }
            
            auditTrailRepository.save(audit);
            log.debug("OTP verification audit logged for user: {}, purpose: {}, successful: {}, attempt: {}", 
                     userId, purpose, successful, attemptNumber);
                     
        } catch (Exception e) {
            log.error("Failed to log OTP verification audit for user: {}", userId, e);
        }
    }

    @Override
    public void logRateLimitExceeded(String userId, String clientIp, String requestId, 
                                    String rateLimitType, int currentCount, int limit) {
        try {
            AuditTrail audit = createBaseAuditTrail(userId, "RATE_LIMIT_EXCEEDED", clientIp, requestId);
            
            audit.setEntityType("SECURITY");
            audit.setEntityId("RATE_LIMIT:" + rateLimitType);
            audit.setChangeDescription("Rate limit exceeded for " + rateLimitType + " - Count: " + currentCount + ", Limit: " + limit);
            audit.setIsSensitiveOperation(true);
            audit.setIsSuccessful(false);
            audit.setRiskLevel("HIGH");
            audit.setErrorMessage("Rate limit exceeded");
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("rateLimitType", rateLimitType);
            metadata.put("currentCount", currentCount);
            metadata.put("limit", limit);
            
            audit.setAdditionalMetadata(objectMapper.writeValueAsString(metadata));
            audit.setTags("security,rate-limit,abuse");
            
            audit.setAlertGenerated(true);
            audit.setAlertLevel("ERROR");
            audit.setAlertMessage("Rate limit exceeded for " + rateLimitType + " by user: " + userId);
            
            auditTrailRepository.save(audit);
            log.warn("Rate limit exceeded audit logged for user: {}, type: {}, count: {}, limit: {}", 
                    userId, rateLimitType, currentCount, limit);
                    
        } catch (Exception e) {
            log.error("Failed to log rate limit audit for user: {}", userId, e);
        }
    }

    @Override
    public void logSuspiciousActivity(String userId, String clientIp, String requestId,
                                     String suspiciousPattern, String riskLevel, String actionTaken) {
        try {
            AuditTrail audit = createBaseAuditTrail(userId, "SUSPICIOUS_ACTIVITY", clientIp, requestId);
            
            audit.setEntityType("SECURITY");
            audit.setEntityId("SUSPICIOUS:" + suspiciousPattern);
            audit.setChangeDescription("Suspicious activity detected: " + suspiciousPattern);
            audit.setIsSensitiveOperation(true);
            audit.setIsSuccessful(false);
            audit.setRiskLevel(riskLevel);
            audit.setErrorMessage("Suspicious activity pattern detected");
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("suspiciousPattern", suspiciousPattern);
            metadata.put("riskLevel", riskLevel);
            metadata.put("actionTaken", actionTaken);
            
            audit.setAdditionalMetadata(objectMapper.writeValueAsString(metadata));
            audit.setTags("security,suspicious,fraud-detection");
            
            audit.setAlertGenerated(true);
            audit.setAlertLevel("CRITICAL".equals(riskLevel) ? "CRITICAL" : "ERROR");
            audit.setAlertMessage("Suspicious activity detected for user: " + userId + " - " + suspiciousPattern);
            
            auditTrailRepository.save(audit);
            log.warn("Suspicious activity audit logged for user: {}, pattern: {}, risk: {}, action: {}", 
                    userId, suspiciousPattern, riskLevel, actionTaken);
                    
        } catch (Exception e) {
            log.error("Failed to log suspicious activity audit for user: {}", userId, e);
        }
    }

    @Override
    public void logSecurityEvent(String userId, String clientIp, String requestId,
                                String eventType, String description, String riskLevel) {
        try {
            AuditTrail audit = createBaseAuditTrail(userId, "SECURITY_EVENT", clientIp, requestId);
            
            audit.setEntityType("SECURITY");
            audit.setEntityId("EVENT:" + eventType);
            audit.setChangeDescription(description);
            audit.setIsSensitiveOperation(true);
            audit.setIsSuccessful(true);
            audit.setRiskLevel(riskLevel);
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("eventType", eventType);
            metadata.put("description", description);
            metadata.put("riskLevel", riskLevel);
            
            audit.setAdditionalMetadata(objectMapper.writeValueAsString(metadata));
            audit.setTags("security,event,monitoring");
            
            if ("HIGH".equals(riskLevel) || "CRITICAL".equals(riskLevel)) {
                audit.setAlertGenerated(true);
                audit.setAlertLevel(riskLevel);
                audit.setAlertMessage("Security event: " + eventType + " for user: " + userId);
            }
            
            auditTrailRepository.save(audit);
            log.info("Security event audit logged for user: {}, event: {}, risk: {}", 
                    userId, eventType, riskLevel);
                    
        } catch (Exception e) {
            log.error("Failed to log security event audit for user: {}", userId, e);
        }
    }

    @Override
    public AuditTrail createBaseAuditTrail(String userId, String operation, String clientIp, String requestId) {
        AuditTrail audit = new AuditTrail();
        
        audit.setAuditId(RequestIdGenerator.generateRequestId());
        audit.setUserId(userId);
        audit.setOperation(operation);
        audit.setSourceIp(clientIp);
        audit.setRequestId(requestId);
        audit.setTimestamp(Instant.now());
        audit.setCreatedAt(Instant.now());
        audit.setSourceSystem("API");
        audit.setApplicationVersion("1.0.0");
        audit.setServerNode(getServerNode());
        audit.setDataClassification("CONFIDENTIAL");
        audit.setContainsPII(true);
        audit.setContainsFinancialData(false);
        audit.setJurisdiction("KE");
        audit.setTimezone("Africa/Nairobi");
        
        return audit;
    }

    private String determineRiskLevel(String purpose) {
        if (purpose.contains("TRANSFER") || purpose.contains("WITHDRAWAL")) {
            return "HIGH";
        } else if (purpose.contains("PIN") || purpose.contains("STATUS")) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private String determineVerificationRiskLevel(boolean successful, int attemptNumber) {
        if (!successful && attemptNumber >= 3) {
            return "CRITICAL";
        } else if (!successful && attemptNumber >= 2) {
            return "HIGH";
        } else if (!successful) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private String getServerNode() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }
}