package com.boit_droid.wallet.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(
    name = "KYCRequest",
    description = "Request object for Know Your Customer (KYC) verification containing identity documents and personal information"
)
public class KYCRequest {
    
    @Schema(
        description = "Type of identity document being submitted for verification",
        example = "ID_CARD",
        requiredMode = Schema.RequiredMode.REQUIRED,
        allowableValues = {"ID_CARD", "PASSPORT", "DRIVING_LICENSE"}
    )
    @NotBlank(message = "Document type is required")
    @Pattern(regexp = "^(ID_CARD|PASSPORT|DRIVING_LICENSE)$", 
             message = "Document type must be ID_CARD, PASSPORT, or DRIVING_LICENSE")
    private String documentType;
    
    @Schema(
        description = "The identification number from the submitted document",
        example = "12345678",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minLength = 5,
        maxLength = 30
    )
    @NotBlank(message = "Document number is required")
    @Size(min = 5, max = 30, message = "Document number must be between 5 and 30 characters")
    private String documentNumber;
    
    @Schema(
        description = "Base64 encoded image of the identity document (JPEG or PNG format)",
        example = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAYEBQYFBAYGBQYHBwYIChAKCgkJChQODwwQFxQYGBcUFhYaHSUfGhsjHBYWICwgIyYnKSopGR8tMC0oMCUoKSj/2wBDAQcHBwoIChMKChMoGhYaKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCj/wAARCAABAAEDASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAv/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/8QAFQEBAQAAAAAAAAAAAAAAAAAAAAX/xAAUEQEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIRAxEAPwCdABmX/9k=",
        requiredMode = Schema.RequiredMode.REQUIRED,
        pattern = "^data:image/(jpeg|jpg|png);base64,[A-Za-z0-9+/=]+$"
    )
    @NotBlank(message = "Document image is required")
//    @Pattern(regexp = "^data:image/(jpeg|jpg|png);base64,[A-Za-z0-9+/=]+$",
//             message = "Document image must be a valid base64 encoded image (JPEG or PNG)")
    private String documentImageBase64;
    
    @Schema(
        description = "Base64 encoded selfie image of the user for identity verification (JPEG or PNG format)",
        example = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAYEBQYFBAYGBQYHBwYIChAKCgkJChQODwwQFxQYGBcUFhYaHSUfGhsjHBYWICwgIyYnKSopGR8tMC0oMCUoKSj/2wBDAQcHBwoIChMKChMoGhYaKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCj/wAARCAABAAEDASIAAhEBAxEB/8QAFQABAQAAAAAAAAAAAAAAAAAAAAv/xAAUEAEAAAAAAAAAAAAAAAAAAAAA/8QAFQEBAQAAAAAAAAAAAAAAAAAAAAX/xAAUEQEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIRAxEAPwCdABmX/9k=",
        requiredMode = Schema.RequiredMode.REQUIRED,
        pattern = "^data:image/(jpeg|jpg|png);base64,[A-Za-z0-9+/=]+$"
    )
    @NotBlank(message = "Selfie image is required")
//    @Pattern(regexp = "^data:image/(jpeg|jpg|png);base64,[A-Za-z0-9+/=]+$",
//             message = "Selfie image must be a valid base64 encoded image (JPEG or PNG)")
    private String selfieImageBase64;
    
    @Schema(
        description = "User's current residential address for verification purposes",
        example = "123 Main Street, Nairobi, Kenya",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minLength = 10,
        maxLength = 200
    )
    @NotBlank(message = "Address is required")
    @Size(min = 10, max = 200, message = "Address must be between 10 and 200 characters")
    private String address;
    
    @Schema(
        description = "User's current occupation or profession",
        example = "Software Developer",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minLength = 2,
        maxLength = 100
    )
    @NotBlank(message = "Occupation is required")
    @Size(min = 2, max = 100, message = "Occupation must be between 2 and 100 characters")
    private String occupation;
    
    @Schema(
        description = "6-digit one-time password for additional security verification. If omitted, an OTP will be sent and the operation will return an error prompting resubmission with the OTP.",
        example = "482913",
        minLength = 6,
        maxLength = 6,
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String otp;
}
