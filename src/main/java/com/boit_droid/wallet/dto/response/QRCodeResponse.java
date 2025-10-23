package com.boit_droid.wallet.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Schema(
    description = "Response object containing QR code information for wallet identification and transactions",
    example = """
    {
        "walletId": "WLT123456789",
        "accountNumber": "ACC001234567890",
        "qrCodeData": "wallet:WLT123456789:ACC001234567890:USD",
        "qrCodeImageBase64": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAASwAAAEsCAYAAAB5fY51AAAAAXNSR0IArs4c6QAAIABJREFUeF7tnQeYFdXVhd+ZO9N7...",
        "generatedAt": "2024-01-15T10:30:00Z",
        "expiresAt": "2024-01-15T22:30:00Z"
    }
    """
)
public class QRCodeResponse {
    
    @Schema(
        description = "Unique identifier of the wallet associated with this QR code",
        example = "WLT123456789",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String walletId;
    
    @Schema(
        description = "Account number of the wallet for identification purposes",
        example = "ACC001234567890",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String accountNumber;
    
    @Schema(
        description = "Raw QR code data string containing wallet identification information",
        example = "wallet:WLT123456789:ACC001234567890:USD",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String qrCodeData;
    
    @Schema(
        description = "Base64 encoded PNG image of the QR code for display purposes",
        example = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAASwAAAEsCAYAAAB5fY51AAAAAXNSR0IArs4c6QAAIABJREFUeF7tnQeYFdXVhd+ZO9N7...",
        requiredMode = Schema.RequiredMode.REQUIRED,
        format = "base64"
    )
    private String qrCodeImageBase64;
    
    @Schema(
        description = "Timestamp when the QR code was generated",
        example = "2024-01-15T10:30:00Z",
        requiredMode = Schema.RequiredMode.REQUIRED,
        format = "date-time"
    )
    private Instant generatedAt;
    
    @Schema(
        description = "Timestamp when the QR code expires (typically 12 hours after generation)",
        example = "2024-01-15T22:30:00Z",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        format = "date-time"
    )
    private Instant expiresAt;
}