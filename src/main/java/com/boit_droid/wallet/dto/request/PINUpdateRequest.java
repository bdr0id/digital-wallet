package com.boit_droid.wallet.dto.request;

import com.boit_droid.wallet.validation.ValidPIN;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(
    description = "Request object for updating wallet PIN with security verification. Requires current PIN verification and confirmation of new PIN to prevent unauthorized changes",
    example = """
    {
        "currentPin": "1357",
        "newPin": "2468",
        "confirmPin": "2468"
    }
    """
)
public class PINUpdateRequest {
    
    @NotBlank(message = "Current PIN is required")
    @ValidPIN
    @Schema(
        description = "Current 4-6 digit PIN for identity verification. Must match the wallet's existing PIN. Failed attempts are tracked for security purposes",
        example = "1357",
        minLength = 4,
        maxLength = 6,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String currentPin;
    
    @NotBlank(message = "New PIN is required")
    @ValidPIN
    @Schema(
        description = "New 4-6 digit PIN to replace the current PIN. Cannot be weak patterns like 1111, 1234, or sequential numbers. Must be different from the current PIN and cannot be any of the last 5 PINs used",
        example = "2468",
        minLength = 4,
        maxLength = 6,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String newPin;
    
    @NotBlank(message = "Confirm PIN is required")
    @ValidPIN
    @Schema(
        description = "Confirmation of the new PIN. Must match the new PIN exactly to prevent typos. This ensures the user knows their new PIN before the change is applied",
        example = "2468",
        minLength = 4,
        maxLength = 6,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String confirmPin;
    
    @AssertTrue(message = "New PIN must match confirm PIN")
    @Schema(hidden = true)
    public boolean isPinConfirmationValid() {
        if (newPin == null || confirmPin == null) {
            return true; // Let @NotBlank handle null validation
        }
        return newPin.equals(confirmPin);
    }
    
    @Schema(
        description = "6-digit one-time password for additional security verification. If omitted, an OTP will be sent and the operation will return an error prompting resubmission with the OTP.",
        example = "482913",
        minLength = 6,
        maxLength = 6,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String otp;
    
    @AssertTrue(message = "New PIN must be different from current PIN")
    @Schema(hidden = true)
    public boolean isPinDifferent() {
        if (currentPin == null || newPin == null) {
            return true; // Let @NotBlank handle null validation
        }
        return !currentPin.equals(newPin);
    }
}