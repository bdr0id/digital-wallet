package com.boit_droid.wallet.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(
    description = "Request object for creating or updating notifications",
    example = """
    {
        "title": "Transaction Alert",
        "message": "You have received $50.00 from John Doe",
        "type": "TRANSACTION",
        "priority": "MEDIUM",
        "channel": "IN_APP",
        "relatedTransactionId": "txn_123456789",
        "relatedWalletId": "wallet_987654321",
        "requiresAcknowledgment": false
    }
    """
)
public class NotificationRequest {
    
    @Schema(
        description = "Notification title - brief summary of the notification content",
        example = "Transaction Alert",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minLength = 1,
        maxLength = 255
    )
    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    private String title;
    
    @Schema(
        description = "Detailed notification message content",
        example = "You have received $50.00 from John Doe",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minLength = 1,
        maxLength = 1000
    )
    @NotBlank(message = "Message is required")
    @Size(min = 1, max = 1000, message = "Message must be between 1 and 1000 characters")
    private String message;
    
    @Schema(
        description = "Type of notification indicating the category or purpose",
        example = "TRANSACTION",
        requiredMode = Schema.RequiredMode.REQUIRED,
        allowableValues = {"TRANSACTION", "KYC", "SECURITY", "PROMOTION", "SYSTEM", "WALLET_STATUS", "BALANCE_ALERT", "LOGIN_ALERT", "PIN_CHANGE", "ACCOUNT_UPDATE"}
    )
    @NotBlank(message = "Type is required")
    @Pattern(regexp = "^(TRANSACTION|KYC|SECURITY|PROMOTION|SYSTEM|WALLET_STATUS|BALANCE_ALERT|LOGIN_ALERT|PIN_CHANGE|ACCOUNT_UPDATE)$", 
             message = "Type must be one of: TRANSACTION, KYC, SECURITY, PROMOTION, SYSTEM, WALLET_STATUS, BALANCE_ALERT, LOGIN_ALERT, PIN_CHANGE, ACCOUNT_UPDATE")
    private String type;
    
    @Schema(
        description = "Priority level of the notification affecting delivery and display order",
        example = "MEDIUM",
        defaultValue = "MEDIUM",
        allowableValues = {"LOW", "MEDIUM", "HIGH", "URGENT", "CRITICAL"}
    )
    @Pattern(regexp = "^(LOW|MEDIUM|HIGH|URGENT|CRITICAL)$", 
             message = "Priority must be LOW, MEDIUM, HIGH, URGENT, or CRITICAL")
    private String priority = "MEDIUM";
    
    @Schema(
        description = "Delivery channel for the notification",
        example = "IN_APP",
        defaultValue = "IN_APP",
        allowableValues = {"EMAIL", "SMS", "PUSH", "IN_APP"}
    )
    @Pattern(regexp = "^(EMAIL|SMS|PUSH|IN_APP)$", 
             message = "Channel must be EMAIL, SMS, PUSH, or IN_APP")
    private String channel = "IN_APP";
    
    @Schema(
        description = "Optional reference to a related transaction ID",
        example = "txn_123456789"
    )
    private String relatedTransactionId;
    
    @Schema(
        description = "Optional reference to a related wallet ID",
        example = "wallet_987654321"
    )
    private String relatedWalletId;
    
    @Schema(
        description = "Optional reference to a notification template ID for consistent formatting",
        example = "template_transaction_received"
    )
    private String templateId;
    
    @Schema(
        description = "Optional JSON string containing additional metadata for the notification",
        example = "{\"amount\": \"50.00\", \"currency\": \"USD\", \"sender\": \"John Doe\"}"
    )
    private String metadata;
    
    @Schema(
        description = "Whether the notification requires explicit user acknowledgment",
        example = "false",
        defaultValue = "false"
    )
    private Boolean requiresAcknowledgment = false;
}