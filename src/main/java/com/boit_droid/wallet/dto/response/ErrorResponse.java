package com.boit_droid.wallet.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(
    name = "ErrorResponse",
    description = "Standard error response format for all API errors",
    example = """
        {
          "error": "VALIDATION_ERROR",
          "message": "Request validation failed",
          "requestId": "550e8400-e29b-41d4-a716-446655440000",
          "path": "/api/v1/users/register",
          "status": 400,
          "timestamp": "2024-01-15T10:30:00Z",
          "details": ["email: must be a valid email address", "password: must be at least 8 characters"]
        }
        """
)
public class ErrorResponse {
    
    @Schema(
        description = "Error code identifying the type of error that occurred",
        example = "VALIDATION_ERROR",
        required = true,
        allowableValues = {
            "VALIDATION_ERROR", "USER_NOT_FOUND", "WALLET_NOT_FOUND", "INSUFFICIENT_FUNDS",
            "INVALID_PIN", "KYC_VERIFICATION_FAILED", "TRANSACTION_FAILED", "WALLET_STATUS_ERROR",
            "CONSTRAINT_VIOLATION", "BINDING_ERROR", "MISSING_PARAMETER", "TYPE_MISMATCH",
            "MALFORMED_REQUEST", "METHOD_NOT_SUPPORTED", "SECURITY_ERROR", "INVALID_ARGUMENT",
            "SECURITY_OPERATION_FAILED", "INTERNAL_SERVER_ERROR"
        }
    )
    private String error;

    @Schema(
        description = "Human-readable error message describing what went wrong",
        example = "Request validation failed",
        required = true
    )
    private String message;

    @Schema(
        description = "Unique identifier for tracking this request across systems",
        example = "550e8400-e29b-41d4-a716-446655440000",
        required = true
    )
    private String requestId;

    @Schema(
        description = "API endpoint path where the error occurred",
        example = "/api/v1/users/register",
        required = true
    )
    private String path;

    @Schema(
        description = "HTTP status code associated with this error",
        example = "400",
        required = true,
        minimum = "400",
        maximum = "599"
    )
    private Integer status;

    @Schema(
        description = "Timestamp when the error occurred",
        example = "2024-01-15T10:30:00Z",
        required = true,
        type = "string",
        format = "date-time"
    )
    private Instant timestamp;

    @Schema(
        description = "Detailed error information, validation messages, or additional context",
        example = "[\"email: must be a valid email address\", \"password: must be at least 8 characters\"]",
        required = false
    )
    private List<String> details;
    
    public ErrorResponse(String error, String message, String requestId, String path, Integer status) {
        this.error = error;
        this.message = message;
        this.requestId = requestId;
        this.path = path;
        this.status = status;
        this.timestamp = Instant.now();
    }
}