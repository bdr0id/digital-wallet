package com.boit_droid.wallet.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(
    name = "ApiResponse",
    description = "Standard API response wrapper for all endpoints",
    example = """
        {
          "success": true,
          "message": "Operation completed successfully",
          "requestId": "550e8400-e29b-41d4-a716-446655440000",
          "locale": "en-US",
          "salt": "abc123def456",
          "signature": "sha256_signature_hash",
          "data": {},
          "errors": null,
          "timestamp": "2024-01-15T10:30:00Z"
        }
        """
)
public class ApiResponse<T> {

    @Schema(
        description = "Indicates whether the operation was successful",
        example = "true",
        required = true
    )
    private Boolean success;

    @Schema(
        description = "Human-readable message describing the operation result",
        example = "User registered successfully",
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
        description = "Locale information for internationalization support",
        example = "en-US",
        required = false
    )
    private String locale;

    @Schema(
        description = "Salt value used for cryptographic operations and request signing",
        example = "abc123def456",
        required = false
    )
    private String salt;

    @Schema(
        description = "Digital signature for request/response integrity verification",
        example = "sha256_signature_hash",
        required = false
    )
    private String signature;

    @Schema(
        description = "Response payload containing the actual data (varies by endpoint)",
        required = false
    )
    private T data;

    @Schema(
        description = "List of error messages when operation fails (null for successful operations)",
        example = "[\"Invalid email format\", \"Password too short\"]",
        required = false
    )
    private List<String> errors;

    @Schema(
        description = "Timestamp when the response was generated",
        example = "2024-01-15T10:30:00Z",
        required = true,
        type = "string",
        format = "date-time"
    )
    private Instant timestamp;
    
    // Constructor for success responses with data
    public ApiResponse(Boolean success, String message, String requestId, T data) {
        this.success = success;
        this.message = message;
        this.requestId = requestId;
        this.data = data;
        this.timestamp = Instant.now();
    }
    
    // Constructor for error responses
    public ApiResponse(Boolean success, String message, String requestId, List<String> errors) {
        this.success = success;
        this.message = message;
        this.requestId = requestId;
        this.errors = errors;
        this.timestamp = Instant.now();
    }
    
    // Static factory methods for common responses
    public static <T> ApiResponse<T> success(String message, String requestId, T data) {
        return new ApiResponse<>(true, message, requestId, data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, UUID.randomUUID().toString(), data);
    }

    public static <T> ApiResponse<T> error(String message, String requestId, List<String> errors) {
        return new ApiResponse<>(false, message, requestId, errors);
    }

    public boolean isSuccess() {
        return success;
    }
}

