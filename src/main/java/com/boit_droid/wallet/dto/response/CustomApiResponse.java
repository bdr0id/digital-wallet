package com.boit_droid.wallet.dto.response;

import com.boit_droid.wallet.util.SignatureUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(
    name = "CustomApiResponse",
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
public class CustomApiResponse<T> {


    @Schema(
        description = "Indicates whether the operation was successful",
        example = "true"
    )
    private Boolean success;

    @Schema(
        description = "Human-readable message describing the operation result",
        example = "User registered successfully"
    )
    private String message;

    @Schema(
        description = "Unique identifier for tracking this request across systems",
        example = "550e8400-e29b-41d4-a716-446655440000"
    )
    private String requestId;

    @Schema(
        description = "Locale information for internationalization support",
        example = "en-US"
    )
    private String locale;

    @Schema(
        description = "Salt value used for cryptographic operations and request signing",
        example = "abc123def456"
    )
    private String salt;

    @Schema(
        description = "Digital signature for request/response integrity verification",
        example = "sha256_signature_hash"
    )
    private String signature;

    @Schema(
        description = "Response payload containing the actual data (varies by endpoint)"
    )
    private T data;

    @Schema(
        description = "List of error messages when operation fails (null for successful operations)",
        example = "[\"Invalid email format\", \"Password too short\"]"
    )
    private List<String> errors;

    @Schema(
        description = "Timestamp when the response was generated",
        example = "2024-01-15T10:30:00Z",
        type = "string",
        format = "date-time"
    )
    private Instant timestamp;
    
    private void ensureSecurityFields() {

        if (this.salt == null || this.salt.isBlank()) {
            this.salt = UUID.randomUUID().toString();
        }
        if (this.signature == null || this.signature.isBlank()) {
            this.signature = UUID.randomUUID().toString();
        }
    }

    // Constructor for success responses with data
    public CustomApiResponse(Boolean success, String message, String requestId, T data) {
        this.success = success;
        this.message = message;
        this.requestId = requestId;
        this.data = data;
        this.timestamp = Instant.now();
        ensureSecurityFields();
    }
    
    // Constructor for error responses
    public CustomApiResponse(Boolean success, String message, String requestId, List<String> errors) {
        this.success = success;
        this.message = message;
        this.requestId = requestId;
        this.errors = errors;
        this.timestamp = Instant.now();
        ensureSecurityFields();
    }
    
    // Static factory methods for common responses
    public static <T> CustomApiResponse<T> success(String message, String requestId, T data) {
        return new CustomApiResponse<>(true, message, requestId, data);
    }

    public static <T> CustomApiResponse<T> success(String message, T data) {
        return new CustomApiResponse<>(true, message, UUID.randomUUID().toString(), data);
    }

    public static <T> CustomApiResponse<T> error(String message, String requestId, List<String> errors) {
        return new CustomApiResponse<>(false, message, requestId, errors);
    }

    /**
     * Factory method for creating OTP-required responses with 202 Accepted status
     * @param message The message to display to the user
     * @param requestId The request ID for tracking
     * @param otpRequiredData The OTP-required response data
     * @return CustomApiResponse with OtpRequiredResponse data
     */
    public static CustomApiResponse<OtpRequiredResponse> otpRequired(String message, String requestId, OtpRequiredResponse otpRequiredData) {
        CustomApiResponse<OtpRequiredResponse> response = new CustomApiResponse<>(false, message, requestId, otpRequiredData);
        response.setErrors(List.of("Provide the 6-digit OTP to complete the operation"));
        return response;
    }

    public boolean isSuccess() {
        return success;
    }
}

