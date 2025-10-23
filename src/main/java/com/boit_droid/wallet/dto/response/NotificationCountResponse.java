package com.boit_droid.wallet.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

@Data
@Schema(
    description = "Response object containing notification count statistics for a user",
    example = """
    {
        "total": 25,
        "unread": 5,
        "read": 20,
        "TRANSACTION": 10,
        "SECURITY": 3,
        "KYC": 2,
        "SYSTEM": 8,
        "PROMOTION": 2,
        "WALLET_STATUS": 0,
        "BALANCE_ALERT": 0,
        "LOGIN_ALERT": 0,
        "PIN_CHANGE": 0,
        "ACCOUNT_UPDATE": 0
    }
    """
)
public class NotificationCountResponse {
    
    @Schema(
        description = "Total number of notifications for the user",
        example = "25",
        minimum = "0"
    )
    private Long total;
    
    @Schema(
        description = "Number of unread notifications",
        example = "5",
        minimum = "0"
    )
    private Long unread;
    
    @Schema(
        description = "Number of read notifications",
        example = "20",
        minimum = "0"
    )
    private Long read;
    
    @Schema(
        description = "Count of transaction-related notifications",
        example = "10",
        minimum = "0"
    )
    private Long TRANSACTION;
    
    @Schema(
        description = "Count of KYC-related notifications",
        example = "2",
        minimum = "0"
    )
    private Long KYC;
    
    @Schema(
        description = "Count of security-related notifications",
        example = "3",
        minimum = "0"
    )
    private Long SECURITY;
    
    @Schema(
        description = "Count of promotional notifications",
        example = "2",
        minimum = "0"
    )
    private Long PROMOTION;
    
    @Schema(
        description = "Count of system notifications",
        example = "8",
        minimum = "0"
    )
    private Long SYSTEM;
    
    @Schema(
        description = "Count of wallet status notifications",
        example = "0",
        minimum = "0"
    )
    private Long WALLET_STATUS;
    
    @Schema(
        description = "Count of balance alert notifications",
        example = "0",
        minimum = "0"
    )
    private Long BALANCE_ALERT;
    
    @Schema(
        description = "Count of login alert notifications",
        example = "0",
        minimum = "0"
    )
    private Long LOGIN_ALERT;
    
    @Schema(
        description = "Count of PIN change notifications",
        example = "0",
        minimum = "0"
    )
    private Long PIN_CHANGE;
    
    @Schema(
        description = "Count of account update notifications",
        example = "0",
        minimum = "0"
    )
    private Long ACCOUNT_UPDATE;
    
    /**
     * Convert this response object to a Map for backward compatibility
     */
    public Map<String, Long> toMap() {
        return Map.of(
        );
    }
    
    /**
     * Create a NotificationCountResponse from a Map
     */
    public static NotificationCountResponse fromMap(Map<String, Long> countMap) {
        NotificationCountResponse response = new NotificationCountResponse();
        response.setTotal(countMap.getOrDefault("total", 0L));
        response.setUnread(countMap.getOrDefault("unread", 0L));
        response.setRead(countMap.getOrDefault("read", 0L));
        response.setTRANSACTION(countMap.getOrDefault("TRANSACTION", 0L));
        response.setKYC(countMap.getOrDefault("KYC", 0L));
        response.setSECURITY(countMap.getOrDefault("SECURITY", 0L));
        response.setPROMOTION(countMap.getOrDefault("PROMOTION", 0L));
        response.setSYSTEM(countMap.getOrDefault("SYSTEM", 0L));
        response.setWALLET_STATUS(countMap.getOrDefault("WALLET_STATUS", 0L));
        response.setBALANCE_ALERT(countMap.getOrDefault("BALANCE_ALERT", 0L));
        response.setLOGIN_ALERT(countMap.getOrDefault("LOGIN_ALERT", 0L));
        response.setPIN_CHANGE(countMap.getOrDefault("PIN_CHANGE", 0L));
        response.setACCOUNT_UPDATE(countMap.getOrDefault("ACCOUNT_UPDATE", 0L));
        return response;
    }
}