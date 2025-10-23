package com.boit_droid.wallet.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Configuration properties for the Digital Wallet application
 * Centralizes all configurable parameters with validation
 */
@Component
@ConfigurationProperties(prefix = "wallet")
@Validated
public class WalletConfigurationProperties {

    /**
     * Security configuration
     */
    private Security security = new Security();

    /**
     * Transaction configuration
     */
    private Transaction transaction = new Transaction();

    /**
     * Notification configuration
     */
    private Notification notification = new Notification();

    /**
     * Audit configuration
     */
    private Audit audit = new Audit();

    // Getters and setters
    public Security getSecurity() {
        return security;
    }

    public void setSecurity(Security security) {
        this.security = security;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public Notification getNotification() {
        return notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }

    public Audit getAudit() {
        return audit;
    }

    public void setAudit(Audit audit) {
        this.audit = audit;
    }

    /**
     * Security-related configuration
     */
    public static class Security {
        @Min(4)
        private int pinMinLength = 4;

        @Min(4)
        private int pinMaxLength = 6;

        @Min(1)
        private int maxLoginAttempts = 3;

        @Min(300)
        private long lockoutDurationSeconds = 900; // 15 minutes

        @NotBlank
        private String saltAlgorithm = "SHA256";

        // Getters and setters
        public int getPinMinLength() {
            return pinMinLength;
        }

        public void setPinMinLength(int pinMinLength) {
            this.pinMinLength = pinMinLength;
        }

        public int getPinMaxLength() {
            return pinMaxLength;
        }

        public void setPinMaxLength(int pinMaxLength) {
            this.pinMaxLength = pinMaxLength;
        }

        public int getMaxLoginAttempts() {
            return maxLoginAttempts;
        }

        public void setMaxLoginAttempts(int maxLoginAttempts) {
            this.maxLoginAttempts = maxLoginAttempts;
        }

        public long getLockoutDurationSeconds() {
            return lockoutDurationSeconds;
        }

        public void setLockoutDurationSeconds(long lockoutDurationSeconds) {
            this.lockoutDurationSeconds = lockoutDurationSeconds;
        }

        public String getSaltAlgorithm() {
            return saltAlgorithm;
        }

        public void setSaltAlgorithm(String saltAlgorithm) {
            this.saltAlgorithm = saltAlgorithm;
        }
    }

    /**
     * Transaction-related configuration
     */
    public static class Transaction {
        @Min(1)
        private double minTransferAmount = 1.0;

        @Min(1)
        private double maxTransferAmount = 1000000.0;

        @Min(1)
        private double minTopUpAmount = 10.0;

        @Min(1)
        private double maxTopUpAmount = 500000.0;

        @NotBlank
        private String defaultCurrency = "KES";

        @Min(1)
        private int statementMaxDays = 365;

        // Getters and setters
        public double getMinTransferAmount() {
            return minTransferAmount;
        }

        public void setMinTransferAmount(double minTransferAmount) {
            this.minTransferAmount = minTransferAmount;
        }

        public double getMaxTransferAmount() {
            return maxTransferAmount;
        }

        public void setMaxTransferAmount(double maxTransferAmount) {
            this.maxTransferAmount = maxTransferAmount;
        }

        public double getMinTopUpAmount() {
            return minTopUpAmount;
        }

        public void setMinTopUpAmount(double minTopUpAmount) {
            this.minTopUpAmount = minTopUpAmount;
        }

        public double getMaxTopUpAmount() {
            return maxTopUpAmount;
        }

        public void setMaxTopUpAmount(double maxTopUpAmount) {
            this.maxTopUpAmount = maxTopUpAmount;
        }

        public String getDefaultCurrency() {
            return defaultCurrency;
        }

        public void setDefaultCurrency(String defaultCurrency) {
            this.defaultCurrency = defaultCurrency;
        }

        public int getStatementMaxDays() {
            return statementMaxDays;
        }

        public void setStatementMaxDays(int statementMaxDays) {
            this.statementMaxDays = statementMaxDays;
        }
    }

    /**
     * Notification-related configuration
     */
    public static class Notification {
        @Min(1)
        private int maxRetryAttempts = 3;

        @Min(1000)
        private long retryDelayMs = 5000;

        @Min(1)
        private int batchSize = 100;

        private boolean enableEmailNotifications = true;

        private boolean enableSmsNotifications = false;

        // Getters and setters
        public int getMaxRetryAttempts() {
            return maxRetryAttempts;
        }

        public void setMaxRetryAttempts(int maxRetryAttempts) {
            this.maxRetryAttempts = maxRetryAttempts;
        }

        public long getRetryDelayMs() {
            return retryDelayMs;
        }

        public void setRetryDelayMs(long retryDelayMs) {
            this.retryDelayMs = retryDelayMs;
        }

        public int getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }

        public boolean isEnableEmailNotifications() {
            return enableEmailNotifications;
        }

        public void setEnableEmailNotifications(boolean enableEmailNotifications) {
            this.enableEmailNotifications = enableEmailNotifications;
        }

        public boolean isEnableSmsNotifications() {
            return enableSmsNotifications;
        }

        public void setEnableSmsNotifications(boolean enableSmsNotifications) {
            this.enableSmsNotifications = enableSmsNotifications;
        }
    }

    /**
     * Audit-related configuration
     */
    public static class Audit {
        private boolean enableDetailedLogging = true;

        private boolean enableTransactionLogging = true;

        private boolean enableSecurityLogging = true;

        @Min(1)
        private int auditRetentionDays = 2555; // 7 years

        @Min(1)
        private int logRotationDays = 30;

        // Getters and setters
        public boolean isEnableDetailedLogging() {
            return enableDetailedLogging;
        }

        public void setEnableDetailedLogging(boolean enableDetailedLogging) {
            this.enableDetailedLogging = enableDetailedLogging;
        }

        public boolean isEnableTransactionLogging() {
            return enableTransactionLogging;
        }

        public void setEnableTransactionLogging(boolean enableTransactionLogging) {
            this.enableTransactionLogging = enableTransactionLogging;
        }

        public boolean isEnableSecurityLogging() {
            return enableSecurityLogging;
        }

        public void setEnableSecurityLogging(boolean enableSecurityLogging) {
            this.enableSecurityLogging = enableSecurityLogging;
        }

        public int getAuditRetentionDays() {
            return auditRetentionDays;
        }

        public void setAuditRetentionDays(int auditRetentionDays) {
            this.auditRetentionDays = auditRetentionDays;
        }

        public int getLogRotationDays() {
            return logRotationDays;
        }

        public void setLogRotationDays(int logRotationDays) {
            this.logRotationDays = logRotationDays;
        }
    }
}