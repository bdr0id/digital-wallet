package com.boit_droid.wallet.dto.request;

import com.boit_droid.wallet.validation.ValidAmount;
import com.boit_droid.wallet.validation.ValidCurrency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(
    description = "Request object for topping up wallet balance via M-Pesa mobile money service. This endpoint simulates M-Pesa integration for adding funds to a wallet account",
    example = """
    {
        "amount": 500.00,
        "currency": "KES",
        "mpesaTransactionId": "QHX12345678",
        "phoneNumber": "254712345678"
    }
    """
)
public class TopUpRequest {
    
    @NotNull(message = "Amount is required")
    @ValidAmount(min = 1.0, max = 1000000.0, scale = 2)
    @Schema(
        description = "Top-up amount with maximum 2 decimal places precision. Must be between 1.00 and 1,000,000.00. The amount will be added to the wallet balance after successful M-Pesa verification",
        example = "500.00",
        minimum = "1.00",
        maximum = "1000000.00",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Double amount;
    
    @NotBlank(message = "Currency is required")
    @ValidCurrency
    @Schema(
        description = "Currency code for the top-up transaction. Must match the wallet's base currency. Supported currencies: USD (US Dollar), KES (Kenyan Shilling), EUR (Euro), GBP (British Pound)",
        example = "KES",
        allowableValues = {"USD", "KES", "EUR", "GBP"},
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String currency;
    
    @NotBlank(message = "M-Pesa transaction ID is required")
    @Size(min = 10, max = 50, message = "M-Pesa transaction ID must be between 10 and 50 characters")
    @Schema(
        description = "Unique M-Pesa transaction identifier obtained from the M-Pesa payment confirmation. This ID is used to verify the payment with M-Pesa systems and prevent duplicate top-ups",
        example = "QHX12345678",
        minLength = 10,
        maxLength = 50,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String mpesaTransactionId;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be between 10 and 15 digits")
    @Schema(
        description = "Phone number used for the M-Pesa transaction (digits only, including country code). This must match the phone number used in the M-Pesa payment. Format: country code + mobile number (e.g., 254712345678 for Kenya)",
        example = "254712345678",
        pattern = "^[0-9]{10,15}$",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String phoneNumber;
}