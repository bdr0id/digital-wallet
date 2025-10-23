package com.boit_droid.wallet.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Application startup listener for initialization and logging
 * Handles startup events and audit trail initialization
 */
@Component
public class ApplicationStartupListener {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationStartupListener.class);
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT_TRAIL");
    private static final Logger securityLogger = LoggerFactory.getLogger("SECURITY_EVENTS");

    @Autowired
    private EnvironmentDetails environmentDetails;

    /**
     * Handle application ready event
     * Log startup information and initialize audit trails
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(ApplicationReadyEvent event) {
        try {
            // Set up MDC for startup logging
            MDC.put("eventType", "APPLICATION_STARTUP");
            MDC.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            // Log environment details
            environmentDetails.logStartupInfo();
            
            // Log application readiness
            logger.info("Digital Wallet API is ready to accept requests");
            logger.info("Environment Summary: {}", environmentDetails.getEnvironmentSummary());
            
            // Audit log for application startup
            auditLogger.info("Digital Wallet Application successfully started and ready");
            
            // Security log for application startup
            securityLogger.info("Application startup completed - Security subsystem initialized");
            
            // Log profile-specific information
            logProfileSpecificInfo();
            
        } catch (Exception e) {
            logger.error("Error during application startup logging", e);
        } finally {
            MDC.clear();
        }
    }

    /**
     * Log profile-specific startup information
     */
    private void logProfileSpecificInfo() {
        if (environmentDetails.isDevelopment()) {
            logger.info("Development mode: Enhanced logging and debugging enabled");
            logger.info("H2 Console: Disabled (MySQL used in dev)");
        } else if (environmentDetails.isProduction()) {
            logger.info("Production mode: Optimized for performance and security");
            logger.warn("Ensure all sensitive configuration is properly externalized");
        } else if (environmentDetails.isTest()) {
            logger.info("Test mode: Using in-memory H2 database");
            logger.info("H2 Console available at: /h2-console");
        }
    }
}