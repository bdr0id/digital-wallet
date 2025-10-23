package com.boit_droid.wallet.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(
    description = "Response object containing comprehensive wallet statement with transaction history and balance summary",
    example = """
    {
        "walletId": "WLT123456789",
        "accountNumber": "ACC001234567890",
        "accountName": "My Savings Account",
        "startDate": "2024-01-01",
        "endDate": "2024-01-31",
        "openingBalance": 1000.00,
        "closingBalance": 1250.75,
        "totalCredits": 500.75,
        "totalDebits": 250.00,
        "transactionCount": 15,
        "transactions": [
            {
                "transactionId": "TXN-20240130-001234",
                "amount": 150.50,
                "currency": "USD",
                "type": "TRANSFER",
                "status": "ACTIVE",
                "description": "Payment for services",
                "timestamp": "2024-01-30T10:15:30Z"
            }
        ],
        "generatedAt": "2024-02-01T09:00:00Z"
    }
    """
)
public class StatementResponse {
    
    @Schema(
        description = "Unique identifier of the wallet for which the statement was generated",
        example = "WLT123456789",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String walletId;
    
    @Schema(
        description = "Account number of the wallet",
        example = "ACC001234567890",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String accountNumber;
    
    @Schema(
        description = "Display name of the wallet account",
        example = "My Savings Account",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String accountName;
    
    @Schema(
        description = "Start date of the statement period",
        example = "2024-01-01",
        requiredMode = Schema.RequiredMode.REQUIRED,
        format = "date"
    )
    private LocalDate startDate;
    
    @Schema(
        description = "End date of the statement period",
        example = "2024-01-31",
        requiredMode = Schema.RequiredMode.REQUIRED,
        format = "date"
    )
    private LocalDate endDate;
    
    @Schema(
        description = "Wallet balance at the beginning of the statement period",
        example = "1000.00",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Double openingBalance;
    
    @Schema(
        description = "Wallet balance at the end of the statement period",
        example = "1250.75",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Double closingBalance;
    
    @Schema(
        description = "Total amount credited to the wallet during the statement period",
        example = "500.75",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Double totalCredits;
    
    @Schema(
        description = "Total amount debited from the wallet during the statement period",
        example = "250.00",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Double totalDebits;
    
    @Schema(
        description = "Total number of transactions during the statement period",
        example = "15",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer transactionCount;
    
    @Schema(
        description = "List of transactions that occurred during the statement period",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private List<TransactionResponse> transactions;
    
    @Schema(
        description = "Timestamp when the statement was generated",
        example = "2024-02-01T09:00:00Z",
        requiredMode = Schema.RequiredMode.REQUIRED,
        format = "date-time"
    )
    private Instant generatedAt;
}