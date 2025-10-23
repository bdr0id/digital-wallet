package com.boit_droid.wallet.dto.request;

import com.boit_droid.wallet.validation.ValidCurrency;
import com.boit_droid.wallet.validation.ValidPIN;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(
    description = "Request object for creating a new digital wallet account. Each user can have multiple wallets with different currencies and purposes",
    example = """
    {
        "accountName": "My Savings Account",
        "currency": "USD",
        "pin": "2468"
    }
    """
)
public class WalletCreationRequest {
    
    @NotBlank(message = "Account name is required")
    @Size(min = 2, max = 100, message = "Account name must be between 2 and 100 characters")
    @Schema(
        description = "User-defined display name for the wallet account. This name helps users identify the wallet's purpose (e.g., 'Savings', 'Business Account', 'Emergency Fund'). Must be unique per user",
        example = "My Savings Account",
        minLength = 2,
        maxLength = 100,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String accountName;
    
    @NotBlank(message = "Currency is required")
    @ValidCurrency
    @Schema(
        description = "Base currency for the wallet. All transactions in this wallet will be processed in this currency. Once set, the currency cannot be changed. Supported currencies: USD (US Dollar), KES (Kenyan Shilling), EUR (Euro), GBP (British Pound)",
        example = "USD",
        allowableValues = {"USD", "KES", "EUR", "GBP"},
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String currency;
    
    @NotBlank(message = "PIN is required")
    @ValidPIN
    @Schema(
        description = "4-6 digit PIN for wallet security and transaction authorization. Must be unique and cannot be weak patterns like 1111, 1234, or sequential numbers. This PIN will be required for all sensitive wallet operations including transfers, withdrawals, and PIN changes",
        example = "2468",
        minLength = 4,
        maxLength = 6,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String pin;
}
