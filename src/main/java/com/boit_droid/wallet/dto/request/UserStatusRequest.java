package com.boit_droid.wallet.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "Request object for updating user status")
public class UserStatusRequest {

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(ACTIVE|INACTIVE|SUSPENDED|BLOCKED)$", 
             message = "Status must be one of: ACTIVE, INACTIVE, SUSPENDED, BLOCKED")
    @Schema(
        description = "New status to set for the user account",
        example = "ACTIVE",
        allowableValues = {"ACTIVE", "INACTIVE", "SUSPENDED", "BLOCKED"}
    )
    private String status;

    @Schema(
        description = "6-digit OTP code for user status update verification",
        example = "123456",
        pattern = "^[0-9]{6}$"
    )
    private String otp;

    @Schema(
        description = "Optional reason for the status change",
        example = "Account suspended due to suspicious activity"
    )
    private String reason;
}