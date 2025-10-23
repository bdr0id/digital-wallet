package com.boit_droid.wallet.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(
    name = "OtpRequiredResponse",
    description = "Response model for operations that require OTP verification",
    example = """
        {
          "purpose": "TRANSFER:WLT_123->WLT_456",
          "message": "Please provide the 6-digit OTP sent to your registered mobile number",
          "channels": ["SMS", "EMAIL"],
          "expirySeconds": 300,
          "nextStepInstructions": "Resubmit the same request with the 'otp' field included"
        }
        """
)
public class OtpRequiredResponse {

    @Schema(
        description = "Purpose identifier for the OTP request, used for tracking and verification",
        example = "TRANSFER:WLT_123->WLT_456"
    )
    private String purpose;

    @Schema(
        description = "Human-readable message explaining the OTP requirement",
        example = "Please provide the 6-digit OTP sent to your registered mobile number"
    )
    private String message;

    @Schema(
        description = "List of channels through which the OTP was sent",
        example = "[\"SMS\", \"EMAIL\", \"PUSH\"]"
    )
    private List<String> channels;

    @Schema(
        description = "Number of seconds until the OTP expires",
        example = "300"
    )
    private int expirySeconds;

    @Schema(
        description = "Instructions for the client on how to proceed with OTP verification",
        example = "Resubmit the same request with the 'otp' field included"
    )
    private String nextStepInstructions;
}