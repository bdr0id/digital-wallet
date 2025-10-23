package com.boit_droid.wallet.dto.response;

import com.boit_droid.wallet.entity.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Schema(
    name = "UserResponse",
    description = "Response object containing user profile information and account status"
)
public class UserResponse {
    
    @Schema(
        description = "Unique identifier for the user",
        example = "usr_1234567890abcdef",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String userId;
    
    @Schema(
        description = "User's first name",
        example = "John",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String firstName;
    
    @Schema(
        description = "User's middle name (optional)",
        example = "Michael",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String middleName;
    
    @Schema(
        description = "User's last name",
        example = "Doe",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String lastName;
    
    @Schema(
        description = "User's gender",
        example = "MALE",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Gender gender;
    
    @Schema(
        description = "Country code for the mobile number (including + sign)",
        example = "+254",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String countryCode;
    
    @Schema(
        description = "User's mobile phone number (digits only, without country code)",
        example = "1234567890",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String mobile;
    
    @Schema(
        description = "User's national identification number",
        example = "12345678",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String idNumber;
    
    @Schema(
        description = "User's email address",
        example = "john.doe@example.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;
    
    @Schema(
        description = "User's preferred locale for localization",
        example = "en_US",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String locale;
    
    @Schema(
        description = "Current status of the user account",
        example = "ACTIVE",
        requiredMode = Schema.RequiredMode.REQUIRED,
        allowableValues = {"ACTIVE", "INACTIVE", "SUSPENDED", "PENDING"}
    )
    private String status;
    
    @Schema(
        description = "Know Your Customer (KYC) verification status",
        example = "VERIFIED",
        requiredMode = Schema.RequiredMode.REQUIRED,
        allowableValues = {"PENDING", "VERIFIED", "REJECTED", "EXPIRED"}
    )
    private String kycStatus;
    
    @Schema(
        description = "Timestamp when the user account was created",
        example = "2024-01-15T10:30:00Z",
        requiredMode = Schema.RequiredMode.REQUIRED,
        format = "date-time"
    )
    private Instant createdAt;
    
    @Schema(
        description = "Timestamp when the user account was last updated",
        example = "2024-01-20T14:45:00Z",
        requiredMode = Schema.RequiredMode.REQUIRED,
        format = "date-time"
    )
    private Instant updatedAt;
}