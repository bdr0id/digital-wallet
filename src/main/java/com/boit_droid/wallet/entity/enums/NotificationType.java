package com.boit_droid.wallet.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    description = "Enumeration of notification types categorizing different kinds of notifications in the wallet system",
    example = "TRANSACTION"
)
public enum NotificationType {
    
    @Schema(description = "Notifications related to financial transactions (transfers, payments, receipts)")
    TRANSACTION,
    
    @Schema(description = "Notifications related to Know Your Customer verification processes")
    KYC,
    
    @Schema(description = "Security-related notifications (login alerts, PIN changes, suspicious activity)")
    SECURITY,
    
    @Schema(description = "Marketing and promotional notifications")
    PROMOTION,
    
    @Schema(description = "System-wide notifications (maintenance, updates, service announcements)")
    SYSTEM,
    
    @Schema(description = "Wallet status change notifications (activation, suspension, closure)")
    WALLET_STATUS,
    
    @Schema(description = "Balance-related alerts (low balance, threshold warnings)")
    BALANCE_ALERT,
    
    @Schema(description = "Login activity notifications")
    LOGIN_ALERT,
    
    @Schema(description = "PIN change confirmation notifications")
    PIN_CHANGE,
    
    @Schema(description = "Account profile update notifications")
    ACCOUNT_UPDATE
}