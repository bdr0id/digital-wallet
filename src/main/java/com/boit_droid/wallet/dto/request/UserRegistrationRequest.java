package com.boit_droid.wallet.dto.request;

import com.boit_droid.wallet.entity.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(
    name = "UserRegistrationRequest",
    description = "Request object for user registration containing all required personal and contact information"
)
public class UserRegistrationRequest {
    
    @Schema(
        description = "User's first name",
        example = "John",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minLength = 2,
        maxLength = 50
    )
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;
    
    @Schema(
        description = "User's middle name (optional)",
        example = "Michael",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        maxLength = 50
    )
    @Size(max = 50, message = "Middle name must not exceed 50 characters")
    private String middleName;
    
    @Schema(
        description = "User's last name",
        example = "Doe",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minLength = 2,
        maxLength = 50
    )
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;
    
    @Schema(
        description = "User's gender",
        example = "MALE",
        requiredMode = Schema.RequiredMode.REQUIRED,
        allowableValues = {"MALE", "FEMALE", "NONBINARY"}
    )
    @NotNull(message = "Gender is required")
    private Gender gender;
    
    @Schema(
        description = "Country code for the mobile number (including + sign)",
        example = "+254",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minLength = 2,
        maxLength = 5,
        pattern = "^\\+[0-9]{1,4}$"
    )
    @NotBlank(message = "Country code is required")
    @Size(min = 2, max = 5, message = "Country code must be between 2 and 5 characters")
    private String countryCode;
    
    @Schema(
        description = "User's mobile phone number (digits only, without country code)",
        example = "1234567890",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minLength = 10,
        maxLength = 15,
        pattern = "^[0-9]{9,15}$"
    )
    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[0-9]{9,15}$", message = "Mobile number must be between 9 and 15 digits")
    private String mobile;
    
    @Schema(
        description = "User's national identification number",
        example = "12345678",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minLength = 5,
        maxLength = 20
    )
    @NotBlank(message = "ID number is required")
    @Size(min = 5, max = 20, message = "ID number must be between 5 and 20 characters")
    private String idNumber;
    
    @Schema(
        description = "User's email address",
        example = "john.doe@example.com",
        requiredMode = Schema.RequiredMode.REQUIRED,
        format = "email"
    )
    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    private String email;
    
    @Schema(
        description = "User's password for account access",
        example = "SecurePassword123!",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minLength = 8,
        maxLength = 100,
        format = "password"
    )
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;

    private String otp;

    @Schema(
        description = "User's preferred locale for localization (optional)",
        example = "en_US",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
        maxLength = 5,
        pattern = "^[a-z]{2}_[A-Z]{2}$"
    )
    @Size(max = 5, message = "Locale must not exceed 5 characters")
    private String locale;
}
