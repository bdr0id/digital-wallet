package com.boit_droid.wallet.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Schema(
    description = "Response object containing notification details and status information",
    example = """
    {
        "notificationId": "notif_123456789",
        "userId": "user_987654321",
        "title": "Transaction Alert",
        "message": "You have received $50.00 from John Doe",
        "type": "TRANSACTION",
        "status": "DELIVERED",
        "priority": "MEDIUM",
        "channel": "IN_APP",
        "relatedTransactionId": "txn_123456789",
        "relatedWalletId": "wallet_987654321",
        "templateId": "template_transaction_received",
        "isRead": false,
        "isDelivered": true,
        "requiresAcknowledgment": false,
        "sentAt": "2024-01-15T10:30:00Z",
        "deliveredAt": "2024-01-15T10:30:01Z",
        "createdAt": "2024-01-15T10:29:59Z",
        "updatedAt": "2024-01-15T10:30:01Z"
    }
    """
)
public class    NotificationResponse {
    
    @Schema(
        description = "Unique identifier for the notification",
        example = "notif_123456789"
    )
    private Long id;
    
    @Schema(
        description = "Request ID for tracking the notification",
        example = "req_123456789"
    )
    private String requestId;
    
    @Schema(
        description = "Unique identifier of the user who received the notification",
        example = "user_987654321"
    )
    private String userId;
    
    @Schema(
        description = "Brief title or subject of the notification",
        example = "Transaction Alert"
    )
    private String title;
    
    @Schema(
        description = "Detailed message content of the notification",
        example = "You have received $50.00 from John Doe"
    )
    private String message;
    
    @Schema(
        description = "Type/category of the notification",
        example = "TRANSACTION",
        allowableValues = {"TRANSACTION", "KYC", "SECURITY", "PROMOTION", "SYSTEM", "WALLET_STATUS", "BALANCE_ALERT", "LOGIN_ALERT", "PIN_CHANGE", "ACCOUNT_UPDATE"}
    )
    private String type;
    
    @Schema(
        description = "Current delivery status of the notification",
        example = "DELIVERED",
        allowableValues = {"PENDING", "SENT", "DELIVERED", "FAILED", "CANCELLED"}
    )
    private String status;
    
    @Schema(
        description = "Priority level affecting delivery order and display prominence",
        example = "MEDIUM",
        allowableValues = {"LOW", "MEDIUM", "HIGH", "URGENT", "CRITICAL"}
    )
    private String priority;
    
    @Schema(
        description = "Delivery channel used for the notification",
        example = "IN_APP",
        allowableValues = {"EMAIL", "SMS", "PUSH", "IN_APP"}
    )
    private String channel;
    
    @Schema(
        description = "Optional reference to a related transaction",
        example = "txn_123456789"
    )
    private String relatedTransactionId;
    
    @Schema(
        description = "Optional reference to a related wallet",
        example = "wallet_987654321"
    )
    private String relatedWalletId;
    
    @Schema(
        description = "Optional reference to the notification template used",
        example = "template_transaction_received"
    )
    private String templateId;
    
    @Schema(
        description = "Whether the notification has been read by the user",
        example = "false"
    )
    private Boolean isRead;
    
    @Schema(
        description = "Whether the notification has been successfully delivered",
        example = "true"
    )
    private Boolean isDelivered;
    
    @Schema(
        description = "Whether the notification requires explicit user acknowledgment",
        example = "false"
    )
    private Boolean requiresAcknowledgment;
    
    @Schema(
        description = "Timestamp when the notification was sent",
        example = "2024-01-15T10:30:00Z"
    )
    private Instant sentAt;
    
    @Schema(
        description = "Timestamp when the notification was delivered",
        example = "2024-01-15T10:30:01Z"
    )
    private Instant deliveredAt;
    
    @Schema(
        description = "Timestamp when the notification was read by the user",
        example = "2024-01-15T10:35:00Z"
    )
    private Instant readAt;
    
    @Schema(
        description = "Timestamp when the notification was acknowledged by the user",
        example = "2024-01-15T10:36:00Z"
    )
    private Instant acknowledgedAt;
    
    @Schema(
        description = "Timestamp when the notification was created",
        example = "2024-01-15T10:29:59Z"
    )
    private Instant createdAt;
    
    @Schema(
        description = "Timestamp when the notification was last updated",
        example = "2024-01-15T10:30:01Z"
    )
    private Instant updatedAt;
}