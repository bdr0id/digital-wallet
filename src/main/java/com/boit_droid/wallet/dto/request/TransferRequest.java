package com.boit_droid.wallet.dto.request;

import com.boit_droid.wallet.validation.ValidAmount;
import com.boit_droid.wallet.validation.ValidCurrency;
import com.boit_droid.wallet.validation.ValidPIN;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(
    description = "Request object for transferring funds between wallets. This operation debits the sender's wallet and credits the recipient's wallet atomically",
    example = """
    {
        "recipientWalletId": "WLT123456789",
        "amount": 150.50,
        "currency": "USD",
        "description": "Payment for services",
        "pin": "1357"
    }
    """
)
public class TransferRequest {
    
    @NotBlank(message = "Recipient wallet ID is required")
    @Size(min = 10, max = 50, message = "Recipient wallet ID must be between 10 and 50 characters")
    @Schema(
        description = "Unique identifier of the recipient's wallet. The recipient wallet must exist, be active, and support the transfer currency. Cannot be the same as the sender's wallet",
        example = "WLT123456789",
        minLength = 10,
        maxLength = 50,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String recipientWalletId;
    
    @NotNull(message = "Amount is required")
    @ValidAmount(min = 1.0, max = 1000000.0, scale = 2)
    @Schema(
        description = "Transfer amount with maximum 2 decimal places precision. Must be between 1.00 and 1,000,000.00. The sender's wallet must have sufficient balance to cover this amount plus any applicable fees",
        example = "150.50",
        minimum = "1.00",
        maximum = "1000000.00",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Double amount;
    
    @NotBlank(message = "Currency is required")
    @ValidCurrency
    @Schema(
        description = "Currency code for the transfer. Must match both sender's and recipient's wallet currencies. Cross-currency transfers are not currently supported. Supported currencies: USD (US Dollar), KES (Kenyan Shilling), EUR (Euro), GBP (British Pound)",
        example = "USD",
        allowableValues = {"USD", "KES", "EUR", "GBP"},
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String currency;
    
    @Size(max = 200, message = "Description must not exceed 200 characters")
    @Schema(
        description = "Optional description or memo for the transfer. This will be visible to both sender and recipient in their transaction history. Useful for invoice references, payment purposes, etc.",
        example = "Payment for services",
        maxLength = 200,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String description;
    
    @NotBlank(message = "PIN is required")
    @ValidPIN
    @Schema(
        description = "4-6 digit PIN for transaction authorization and security verification. Must be the sender's current wallet PIN. Cannot be weak patterns like 1111, 1234, or sequential numbers. PIN verification is required for all transfer operations",
        example = "1357",
        minLength = 4,
        maxLength = 6,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String pin;

    @Schema(
        description = "6-digit one-time password for additional transaction verification. If omitted, an OTP will be sent and the operation will return an error prompting resubmission with the OTP.",
        example = "482913",
        minLength = 6,
        maxLength = 6,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String otp;
}