package com.boit_droid.wallet.dto.request;

import com.boit_droid.wallet.validation.ValidPIN;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(
    name = "WalletDeletionRequest",
    description = "Request model for wallet deletion operations",
    example = """
        {
          "pin": "1234",
          "otp": "482913"
        }
        """
)
public class WalletDeletionRequest {

    @NotBlank(message = "PIN is required")
    @ValidPIN
    @Schema(
        description = "Wallet PIN for verification before deletion",
        example = "1234",
        required = true
    )
    private String pin;

    @Schema(
        description = "6-digit OTP code for wallet deletion verification. Required for completing the deletion process.",
        example = "482913",
        pattern = "^[0-9]{6}$"
    )
    private String otp;
}