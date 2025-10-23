package com.boit_droid.wallet.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Schema(
    description = "Response object containing comprehensive wallet account information including balance, status, and QR code",
    example = """
    {
        "walletId": "WLT123456789",
        "accountNumber": "ACC001234567890",
        "accountName": "My Savings Account",
        "balance": 1250.75,
        "currency": "USD",
        "status": "ACTIVE",
        "qrCode": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAASwAAAEsCAYAAAB5fY51AAAAAXNSR0IArs4c6QAAIABJREFUeF7tnQeYFdXVhd+ZO9N7...",
        "createdAt": "2024-01-15T10:30:00Z",
        "updatedAt": "2024-01-20T14:45:30Z"
    }
    """
)
public class WalletResponse {
    
    @Schema(
        description = "Unique wallet identifier used for all wallet operations and transactions",
        example = "WLT123456789",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String walletId;
    
    @Schema(
        description = "Unique account number for the wallet, used for external references and transfers",
        example = "ACC001234567890",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String accountNumber;
    
    @Schema(
        description = "User-defined display name for the wallet account for easy identification",
        example = "My Savings Account",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minLength = 2,
        maxLength = 100
    )
    private String accountName;
    
    @Schema(
        description = "Current available balance in the wallet with 2 decimal places precision. This is the amount available for transactions",
        example = "1250.75",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minimum = "0.00"
    )
    private Double balance;
    
    @Schema(
        description = "Base currency code for the wallet. All transactions in this wallet are processed in this currency",
        example = "USD",
        requiredMode = Schema.RequiredMode.REQUIRED,
        allowableValues = {"USD", "KES", "EUR", "GBP"}
    )
    private String currency;
    
    @Schema(
        description = "Current operational status of the wallet. ACTIVE wallets can perform all operations, INACTIVE wallets are temporarily disabled, SUSPENDED wallets are blocked due to security concerns, CLOSED wallets are permanently deactivated",
        example = "ACTIVE",
        requiredMode = Schema.RequiredMode.REQUIRED,
        allowableValues = {"ACTIVE", "INACTIVE", "SUSPENDED", "CLOSED"}
    )
    private String status;
    
    @Schema(
        description = "Base64 encoded PNG image of the wallet's QR code for easy identification and transactions. Can be scanned by other users to initiate transfers",
        example = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAASwAAAEsCAYAAAB5fY51AAAAAXNSR0IArs4c6QAAIABJREFUeF7tnQeYFdXVhd+ZO9N7...",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        format = "base64"
    )
    private String qrCode;
    
    @Schema(
        description = "Timestamp when the wallet was initially created",
        example = "2024-01-15T10:30:00Z",
        requiredMode = Schema.RequiredMode.REQUIRED,
        format = "date-time"
    )
    private Instant createdAt;
    
    @Schema(
        description = "Timestamp when the wallet information was last updated (balance changes, status changes, etc.)",
        example = "2024-01-20T14:45:30Z",
        requiredMode = Schema.RequiredMode.REQUIRED,
        format = "date-time"
    )
    private Instant updatedAt;
}