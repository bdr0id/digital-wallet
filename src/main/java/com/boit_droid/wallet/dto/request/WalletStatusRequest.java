package com.boit_droid.wallet.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(
    description = "Request object for updating wallet status with PIN verification",
    example = """
    {
        "status": "ACTIVE",
        "pin": "1357",
        "reason": "Reactivating wallet after security review"
    }
    """
)
public class WalletStatusRequest {
    
    @Schema(
        description = "New status to set for the wallet",
        example = "ACTIVE",
        requiredMode = Schema.RequiredMode.REQUIRED,
        allowableValues = {"ACTIVE", "INACTIVE", "LOCKED"}
    )
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(ACTIVE|INACTIVE|LOCKED)$", 
             message = "Status must be ACTIVE, INACTIVE, or LOCKED")
    private String status;
    
    @Schema(
        description = "Wallet PIN for verification (4-6 digits)",
        example = "1357",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minLength = 4,
        maxLength = 6,
        pattern = "^[0-9]{4,6}$"
    )
    @NotBlank(message = "PIN is required for status change")
    @Pattern(regexp = "^[0-9]{4,6}$", message = "PIN must be 4 to 6 digits")
    private String pin;
    
    @Schema(
        description = "Optional reason for the status change",
        example = "Reactivating wallet after security review",
        maxLength = 200
    )
    @Size(max = 200, message = "Reason must not exceed 200 characters")
    private String reason;
    
    @Schema(
        description = "6-digit one-time password for additional security verification. If omitted, an OTP will be sent and the operation will return an error prompting resubmission with the OTP.",
        example = "482913",
        minLength = 6,
        maxLength = 6,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String otp;
}