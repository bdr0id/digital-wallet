package com.boit_droid.wallet.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Environment-specific configuration details
 * Provides information about the current deployment environment
 */
@Component
@Data
public class EnvironmentDetails {

    private static final Logger logger = LoggerFactory.getLogger(EnvironmentDetails.class);
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT_TRAIL");

    @Value("${app.server.name}")
    private String appServerName;

    @Value("${app.server.detail}")
    private String appServerDetail;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPassword;
    
    @Value("${spring.profiles.active:default}")
    private String activeProfile;
    
    @Value("${spring.application.name:digital-wallet-api}")
    private String applicationName;
    
    @Value("${server.port:5000}")
    private int serverPort;
    
    @Value("${spring.datasource.hikari.maximum-pool-size:20}")
    private int maxPoolSize;
    
    @Value("${spring.datasource.hikari.minimum-idle:5}")
    private int minIdleConnections;

    public EnvironmentDetails() {
        // Log application startup with audit trail
        logger.info("Digital Wallet Application starting up...");
    }
    
    /**
     * Check if running in development environment
     */
    public boolean isDevelopment() {
        return "dev".equals(activeProfile);
    }
    
    /**
     * Check if running in production environment
     */
    public boolean isProduction() {
        return "prod".equals(activeProfile);
    }
    
    /**
     * Check if running in test environment
     */
    public boolean isTest() {
        return "test".equals(activeProfile);
    }
    
    /**
     * Get environment summary for logging
     */
    public String getEnvironmentSummary() {
        return String.format("Environment: %s (%s) - %s:%d | DB Pool: %d-%d", 
            appServerName, activeProfile, applicationName, serverPort, 
            minIdleConnections, maxPoolSize);
    }
    
    /**
     * Log application startup information
     */
    public void logStartupInfo() {
        logger.info("=== Digital Wallet Application Started ===");
        logger.info("Profile: {}", activeProfile);
        logger.info("Server: {} on port {}", appServerName, serverPort);
        logger.info("Database URL: {}", maskSensitiveUrl(dbUrl));
        logger.info("Connection Pool: {}-{} connections", minIdleConnections, maxPoolSize);
        logger.info("Application: {}", applicationName);
        
        // Audit log for application startup
        MDC.put("eventType", "APPLICATION_STARTUP");
        auditLogger.info("Application started - Profile: {}, Server: {}", activeProfile, appServerName);
        MDC.clear();
    }
    
    /**
     * Mask sensitive information in database URL for logging
     */
    private String maskSensitiveUrl(String url) {
        if (url == null) return "N/A";
        // Remove password and other sensitive parameters from URL
        return url.replaceAll("password=[^&]*", "password=***")
                 .replaceAll("user=[^&]*", "user=***");
    }
}
