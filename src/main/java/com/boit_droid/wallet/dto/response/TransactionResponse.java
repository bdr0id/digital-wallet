package com.boit_droid.wallet.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Schema(
    description = "Comprehensive transaction details response containing all transaction information including parties, amounts, and status",
    example = """
    {
        "transactionId": "TXN-20240130-001234",
        "senderWalletId": "WLT-USR001-001",
        "receiverWalletId": "WLT-USR002-001",
        "senderAccountNumber": "ACC-001234567",
        "receiverAccountNumber": "ACC-007654321",
        "amount": 1500.50,
        "currency": "KES",
        "type": "TRANSFER",
        "status": "ACTIVE",
        "description": "Payment for services",
        "reference": "REF-EXT-123456",
        "timestamp": "2024-01-30T10:15:30Z",
        "createdAt": "2024-01-30T10:15:25Z"
    }
    """
)
public class TransactionResponse {
    
    @Schema(
        description = "Unique transaction identifier used for tracking and reference purposes",
        example = "TXN-20240130-001234",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String transactionId;
    
    @Schema(
        description = "Wallet ID of the sender/source wallet. For deposits and top-ups, this may be null or system-generated",
        example = "WLT-USR001-001",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String senderWalletId;
    
    @Schema(
        description = "Wallet ID of the receiver/destination wallet. For withdrawals, this may be null or system-generated",
        example = "WLT-USR002-001",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String receiverWalletId;
    
    @Schema(
        description = "Account number of the sender wallet for external reference and reconciliation",
        example = "ACC-001234567",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String senderAccountNumber;
    
    @Schema(
        description = "Account number of the receiver wallet for external reference and reconciliation",
        example = "ACC-007654321",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String receiverAccountNumber;
    
    @Schema(
        description = "Transaction amount with up to 2 decimal places precision. Always positive regardless of transaction type",
        example = "1500.50",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minimum = "0.01"
    )
    private Double amount;
    
    @Schema(
        description = "Currency code in which the transaction was processed",
        example = "KES",
        requiredMode = Schema.RequiredMode.REQUIRED,
        allowableValues = {"USD", "KES", "EUR", "GBP"}
    )
    private String currency;
    
    @Schema(
        description = "Type of transaction indicating the nature of the operation. DEPOSIT: money added to wallet, WITHDRAWAL: money removed from wallet, TRANSFER: money moved between wallets, TOP_UP: wallet funded via external payment, REFUND: money returned from previous transaction, REVERSAL: transaction undone due to error, FEE: service charge, INTEREST: earnings on balance, BONUS: promotional credit, PENALTY: charge for violation",
        example = "TRANSFER",
        requiredMode = Schema.RequiredMode.REQUIRED,
        allowableValues = {"DEPOSIT", "WITHDRAWAL", "TRANSFER", "TOP_UP", "REFUND", "REVERSAL", "FEE", "INTEREST", "BONUS", "PENALTY"}
    )
    private String type;
    
    @Schema(
        description = "Current processing status of the transaction. ACTIVE: successfully completed, PENDING: awaiting processing or approval, LOCKED: temporarily held for review, CLOSED: completed and finalized, TIMEOUT: failed due to processing timeout, CANCELLED: cancelled by user or system",
        example = "ACTIVE",
        requiredMode = Schema.RequiredMode.REQUIRED,
        allowableValues = {"ACTIVE", "PENDING", "LOCKED", "CLOSED", "TIMEOUT", "CANCELLED"}
    )
    private String status;
    
    @Schema(
        description = "Optional user-provided description or memo for the transaction",
        example = "Payment for services",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        maxLength = 200
    )
    private String description;
    
    @Schema(
        description = "External reference number for tracking with third-party systems or user records",
        example = "REF-EXT-123456",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String reference;
    
    @Schema(
        description = "Timestamp when the transaction was successfully processed and completed",
        example = "2024-01-30T10:15:30Z",
        requiredMode = Schema.RequiredMode.REQUIRED,
        format = "date-time"
    )
    private Instant timestamp;
    
    @Schema(
        description = "Timestamp when the transaction was initially created in the system",
        example = "2024-01-30T10:15:25Z",
        requiredMode = Schema.RequiredMode.REQUIRED,
        format = "date-time"
    )
    private Instant createdAt;
}