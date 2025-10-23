package com.boit_droid.wallet.controller;

import com.boit_droid.wallet.dto.request.KYCRequest;
import com.boit_droid.wallet.dto.request.UserRegistrationRequest;
import com.boit_droid.wallet.dto.request.UserStatusRequest;
import com.boit_droid.wallet.dto.response.CustomApiResponse;
import com.boit_droid.wallet.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User registration, KYC verification, and profile management")
public class UserController {

    private final UserService userService;

    @Operation(
        summary = "Register a new user",
        description = "Creates a new user account with the provided information and automatically creates an associated wallet. " +
                     "This endpoint validates all user information and ensures unique mobile numbers and email addresses.",
        tags = {"Users"}
    )
   @ApiResponses(value = {
           @ApiResponse(
                   responseCode = "201",
                   description = "User successfully registered",
                   content = @Content(
                           mediaType = "application/json",
                           schema = @Schema(implementation = CustomApiResponse.class),
                           examples = @ExampleObject(
                                   name = "Successful Registration",
                                   summary = "User registration successful",
                                   value = """
                                           {
                                             "success": true,
                                             "message": "User registered successfully",
                                             "requestId": "req_123456789",
                                             "data": {
                                               "userId": "usr_987654321",
                                               "firstName": "John",
                                               "lastName": "Doe",
                                               "email": "john.doe@example.com",
                                               "mobile": "1234567890",
                                               "status": "ACTIVE",
                                               "walletId": "wal_456789123"
                                             },
                                             "timestamp": "2024-01-15T10:30:00Z"
                                           }
                                           """
                           )
                   )
           ),
           @ApiResponse(
                   responseCode = "202",
                   description = "Accepted - OTP required for user registration",
                   content = @Content(
                           mediaType = "application/json",
                           schema = @Schema(implementation = CustomApiResponse.class),
                           examples = @ExampleObject(
                                   name = "OTP Required for Registration",
                                   summary = "OTP verification required to complete user registration",
                                   value = """
                                           {
                                             "success": false,
                                             "message": "OTP required. Code sent to your registered mobile/email.",
                                             "requestId": "req_123456789",
                                             "locale": null,
                                             "salt": "f57634bd-53e9-4d7f-b2d4-8d469d29594d",
                                             "signature": "1a82799c-87eb-41db-895e-6d07567419f9",
                                             "data": {
                                               "purpose": "USER_REGISTRATION:1234567890",
                                               "message": "Please provide the 6-digit OTP sent to your registered mobile number",
                                               "channels": ["SMS", "EMAIL"],
                                               "expirySeconds": 300,
                                               "nextStepInstructions": "Resubmit the same request with the 'otp' field included"
                                             },
                                             "errors": ["Provide the 6-digit OTP to complete the user registration"],
                                             "timestamp": "2024-01-15T10:30:00Z"
                                           }
                                           """
                           )
                   )
           ),
           @ApiResponse(
                   responseCode = "400",
                   description = "Bad Request - Validation failed, user already exists, or invalid OTP",
                   content = @Content(
                           mediaType = "application/json",
                           schema = @Schema(implementation = CustomApiResponse.class),
                           examples = {
                                   @ExampleObject(
                                           name = "Validation Error",
                                           summary = "Registration validation failed",
                                           value = """
                                                   {
                                                     "success": false,
                                                     "message": "Validation failed",
                                                     "requestId": "req_123456789",
                                                     "errors": [
                                                       "Email is required",
                                                       "Mobile number must be between 10 and 15 digits",
                                                       "Password must be between 8 and 100 characters"
                                                     ],
                                                     "timestamp": "2024-01-15T10:30:00Z"
                                                   }
                                                   """
                                   ),
                                   @ExampleObject(
                                           name = "Invalid OTP Error",
                                           summary = "Invalid or expired OTP provided",
                                           value = """
                                                   {
                                                     "success": false,
                                                     "message": "Invalid or expired OTP",
                                                     "requestId": "req_123456789",
                                                     "errors": [
                                                       "Please request a new OTP and try again"
                                                     ],
                                                     "timestamp": "2024-01-15T10:30:00Z"
                                                   }
                                                   """
                                   )
                           }
                   )
           ),
           @ApiResponse(
                   responseCode = "409",
                   description = "Conflict - User with email or mobile already exists",
                   content = @Content(
                           mediaType = "application/json",
                           schema = @Schema(implementation = CustomApiResponse.class),
                           examples = @ExampleObject(
                                   name = "User Already Exists",
                                   summary = "User with provided details already exists",
                                   value = """
                                           {
                                             "success": false,
                                             "message": "User already exists",
                                             "requestId": "req_123456789",
                                             "errors": [
                                               "User with email john.doe@example.com already exists"
                                             ],
                                             "timestamp": "2024-01-15T10:30:00Z"
                                           }
                                           """
                           )
                   )
           ),
           @ApiResponse(responseCode = "500", description = "Internal Server Error - Unexpected system error occurred")
   })
    @PostMapping("/register")
    public ResponseEntity<CustomApiResponse> registerUser(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "User registration information including personal details, contact information, and credentials",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserRegistrationRequest.class),
                examples = @ExampleObject(
                    name = "User Registration Example",
                    summary = "Complete user registration request",
                    value = """
                        {
                          "firstName": "John",
                          "middleName": "Michael",
                          "lastName": "Doe",
                          "gender": "MALE",
                          "countryCode": "+254",
                          "mobile": "1234567890",
                          "idNumber": "12345678",
                          "email": "john.doe@example.com",
                          "password": "SecurePassword123!",
                          "locale": "en_US"
                        }
                        """
                )
            )
        )
        @Valid @RequestBody UserRegistrationRequest request) {
        log.info("Received user registration request for mobile: {}", request.getMobile());
        
        CustomApiResponse response = userService.registerUser(request);
        
        // Determine HTTP status based on response
        HttpStatus status;
        if (response.getSuccess()) {
            status = HttpStatus.CREATED; // 201 - User registered successfully
        } else if (response.getMessage() != null && response.getMessage().contains("OTP required")) {
            status = HttpStatus.ACCEPTED; // 202 - OTP required
        } else {
            status = HttpStatus.BAD_REQUEST; // 400 - Validation error or invalid OTP
        }
        
        return ResponseEntity.status(status).body(response);
    }

    @Operation(
        summary = "Perform KYC verification for a user",
        description = "Submits Know Your Customer (KYC) verification documents and information for a registered user. " +
                     "This endpoint processes identity documents, selfie images, and personal information to verify user identity. " +
                     "OTP verification is required for this sensitive operation.",
        tags = {"Users"}
    )
   @ApiResponses(value = {
       @ApiResponse(
           responseCode = "200",
           description = "KYC verification submitted successfully",
           content = @Content(
               mediaType = "application/json",
               schema = @Schema(implementation = CustomApiResponse.class),
               examples = @ExampleObject(
                   name = "KYC Verification Success",
                   summary = "KYC verification submitted successfully",
                   value = """
                       {
                         "success": true,
                         "message": "KYC verification submitted successfully",
                         "requestId": "req_123456789",
                         "data": {
                           "userId": "usr_987654321",
                           "kycStatus": "PENDING_REVIEW",
                           "documentType": "ID_CARD",
                           "submissionDate": "2024-01-15T10:30:00Z",
                           "reviewEstimate": "2-3 business days"
                         },
                         "timestamp": "2024-01-15T10:30:00Z"
                       }
                       """
               )
           )
       ),
       @ApiResponse(
           responseCode = "202",
           description = "Accepted - OTP required for KYC verification",
           content = @Content(
               mediaType = "application/json",
               schema = @Schema(implementation = CustomApiResponse.class),
               examples = @ExampleObject(
                   name = "OTP Required for KYC",
                   summary = "OTP verification required to proceed with KYC",
                   value = """
                       {
                         "success": false,
                         "message": "OTP required. Code sent to registered channels.",
                         "requestId": "req_123456789",
                         "locale": null,
                         "salt": "f57634bd-53e9-4d7f-b2d4-8d469d29594d",
                         "signature": "1a82799c-87eb-41db-895e-6d07567419f9",
                         "data": {
                           "purpose": "KYC_VERIFICATION:usr_987654321",
                           "message": "Please provide the 6-digit OTP sent to your registered mobile number",
                           "channels": ["SMS", "EMAIL"],
                           "expirySeconds": 300,
                           "nextStepInstructions": "Resubmit the same request with the 'otp' field included"
                         },
                         "errors": ["Provide the 6-digit OTP to complete the KYC verification"],
                         "timestamp": "2024-01-15T10:30:00Z"
                       }
                       """
               )
           )
       ),
       @ApiResponse(
           responseCode = "400",
           description = "Bad Request - Invalid KYC data, invalid OTP, or user not eligible",
           content = @Content(
               mediaType = "application/json",
               schema = @Schema(implementation = CustomApiResponse.class),
               examples = {
                   @ExampleObject(
                       name = "KYC Validation Error",
                       summary = "KYC validation failed",
                       value = """
                           {
                             "success": false,
                             "message": "KYC validation failed",
                             "requestId": "req_123456789",
                             "errors": [
                               "Document image must be a valid base64 encoded image",
                               "Address must be between 10 and 200 characters",
                               "Document type must be ID_CARD, PASSPORT, or DRIVING_LICENSE"
                             ],
                             "timestamp": "2024-01-15T10:30:00Z"
                           }
                           """
                   ),
                   @ExampleObject(
                       name = "Invalid OTP Error",
                       summary = "Invalid or expired OTP provided",
                       value = """
                           {
                             "success": false,
                             "message": "Invalid or expired OTP",
                             "requestId": "req_123456789",
                             "errors": [
                               "Please request a new OTP and try again"
                             ],
                             "timestamp": "2024-01-15T10:30:00Z"
                           }
                           """
                   )
               }
           )
       ),
       @ApiResponse(responseCode = "404", description = "User not found"),
       @ApiResponse(responseCode = "500", description = "Internal Server Error - Unexpected system error occurred")
   })
    @PostMapping("/{userId}/kyc")
    public ResponseEntity<CustomApiResponse> performKYC(
            @Parameter(
                name = "userId",
                description = "Unique identifier of the user for KYC verification",
                required = true,
                example = "usr_987654321",
                schema = @Schema(type = "string")
            )
            @PathVariable String userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "KYC verification documents and information including identity document, selfie, and personal details. " +
                             "Include the 'otp' field if you have received an OTP code for this operation.",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = KYCRequest.class),
                    examples = {
                        @ExampleObject(
                            name = "KYC Request Example",
                            summary = "Complete KYC verification request",
                            value = """
                                {
                                  "documentType": "ID_CARD",
                                  "documentNumber": "12345678",
                                  "documentImageBase64": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQ...",
                                  "selfieImageBase64": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQ...",
                                  "address": "123 Main Street, Nairobi, Kenya",
                                  "occupation": "Software Developer"
                                }
                                """
                        ),
                        @ExampleObject(
                            name = "KYC Request with OTP",
                            summary = "KYC verification request with OTP code",
                            value = """
                                {
                                  "documentType": "ID_CARD",
                                  "documentNumber": "12345678",
                                  "documentImageBase64": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQ...",
                                  "selfieImageBase64": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQ...",
                                  "address": "123 Main Street, Nairobi, Kenya",
                                  "occupation": "Software Developer",
                                  "otp": "123456"
                                }
                                """
                        )
                    }
                )
            )
            @Valid @RequestBody KYCRequest request) {
        log.info("Received KYC verification request for user: {}", userId);
        
        CustomApiResponse response = userService.performKYC(userId, request);
        
        // Determine HTTP status based on response
        HttpStatus status;
        if (response.getSuccess()) {
            status = HttpStatus.OK; // 200 - KYC processed successfully
        } else if (response.getMessage() != null && response.getMessage().contains("OTP required")) {
            status = HttpStatus.ACCEPTED; // 202 - OTP required
        } else {
            status = HttpStatus.BAD_REQUEST; // 400 - Validation error or invalid OTP
        }
        
        return ResponseEntity.status(status).body(response);
    }

    @Operation(
        summary = "Get user profile information",
        description = "Retrieves comprehensive profile information for a specific user including personal details, " +
                     "account status, KYC verification status, and associated wallet information.",
        tags = {"Users"}
    )
   @ApiResponses(value = {
       @ApiResponse(
           responseCode = "200",
           description = "User profile retrieved successfully",
           content = @Content(
               mediaType = "application/json",
               schema = @Schema(implementation = CustomApiResponse.class),
               examples = @ExampleObject(
                   name = "User Profile Success",
                   summary = "User profile retrieved successfully",
                   value = """
                       {
                         "success": true,
                         "message": "User profile retrieved successfully",
                         "requestId": "req_123456789",
                         "data": {
                           "userId": "usr_987654321",
                           "firstName": "John",
                           "middleName": "Michael",
                           "lastName": "Doe",
                           "email": "john.doe@example.com",
                           "mobile": "1234567890",
                           "gender": "MALE",
                           "countryCode": "+254",
                           "idNumber": "12345678",
                           "status": "ACTIVE",
                           "kycStatus": "VERIFIED",
                           "locale": "en_US",
                           "createdAt": "2024-01-10T08:00:00Z",
                           "updatedAt": "2024-01-15T10:30:00Z"
                         },
                         "timestamp": "2024-01-15T10:30:00Z"
                       }
                       """
               )
           )
       ),
       @ApiResponse(
           responseCode = "404",
           description = "User not found",
           content = @Content(
               mediaType = "application/json",
               schema = @Schema(implementation = CustomApiResponse.class),
               examples = @ExampleObject(
                   name = "User Not Found",
                   summary = "User with specified ID not found",
                   value = """
                       {
                         "success": false,
                         "message": "User not found",
                         "requestId": "req_123456789",
                         "errors": [
                           "User with ID usr_987654321 not found"
                         ],
                         "timestamp": "2024-01-15T10:30:00Z"
                       }
                       """
               )
           )
       ),
       @ApiResponse(responseCode = "500", description = "Internal Server Error - Unexpected system error occurred")
   })
    @GetMapping("/{userId}/profile")
    public ResponseEntity<CustomApiResponse> getUserProfile(
            @Parameter(
                name = "userId",
                description = "Unique identifier of the user whose profile is to be retrieved",
                required = true,
                example = "usr_987654321",
                schema = @Schema(type = "string")
            )
            @PathVariable String userId) {
        log.info("Received request to get user profile for user: {}", userId);
        
        CustomApiResponse response = userService.getUserProfile(userId);
        
        HttpStatus status = response.getSuccess() ? HttpStatus.OK : HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status).body(response);
    }

    @Operation(
        summary = "Update user status",
        description = "Updates the status of a specific user account. This endpoint allows administrators to activate, " +
                     "deactivate, suspend, or change the status of user accounts for account management purposes. " +
                     "OTP verification is required for this sensitive operation.",
        tags = {"Users"}
    )
   @ApiResponses(value = {
       @ApiResponse(
           responseCode = "200",
           description = "User status updated successfully",
           content = @Content(
               mediaType = "application/json",
               schema = @Schema(implementation = CustomApiResponse.class),
               examples = @ExampleObject(
                   name = "Status Update Success",
                   summary = "User status updated successfully",
                   value = """
                       {
                         "success": true,
                         "message": "User status updated successfully from ACTIVE to SUSPENDED",
                         "requestId": "req_123456789",
                         "data": {
                           "userId": "usr_987654321",
                           "firstName": "John",
                           "lastName": "Doe",
                           "email": "john.doe@example.com",
                           "mobile": "1234567890",
                           "status": "SUSPENDED",
                           "previousStatus": "ACTIVE",
                           "updatedAt": "2024-01-15T10:30:00Z",
                           "reason": "Account suspended due to suspicious activity"
                         },
                         "timestamp": "2024-01-15T10:30:00Z"
                       }
                       """
               )
           )
       ),
       @ApiResponse(
           responseCode = "202",
           description = "Accepted - OTP required for user status update",
           content = @Content(
               mediaType = "application/json",
               schema = @Schema(implementation = CustomApiResponse.class),
               examples = @ExampleObject(
                   name = "OTP Required for Status Update",
                   summary = "OTP verification required to proceed with status update",
                   value = """
                       {
                         "success": false,
                         "message": "OTP required. Code sent to registered channels.",
                         "requestId": "req_123456789",
                         "locale": null,
                         "salt": "f57634bd-53e9-4d7f-b2d4-8d469d29594d",
                         "signature": "1a82799c-87eb-41db-895e-6d07567419f9",
                         "data": {
                           "purpose": "USER_STATUS_UPDATE:usr_987654321",
                           "message": "Please provide the 6-digit OTP sent to your registered mobile number",
                           "channels": ["SMS", "EMAIL"],
                           "expirySeconds": 300,
                           "nextStepInstructions": "Resubmit the same request with the 'otp' field included"
                         },
                         "errors": ["Provide the 6-digit OTP to complete the user status update"],
                         "timestamp": "2024-01-15T10:30:00Z"
                       }
                       """
               )
           )
       ),
       @ApiResponse(
           responseCode = "400",
           description = "Bad Request - Invalid status value, invalid OTP, or operation not allowed",
           content = @Content(
               mediaType = "application/json",
               schema = @Schema(implementation = CustomApiResponse.class),
               examples = {
                   @ExampleObject(
                       name = "Invalid Status Error",
                       summary = "Invalid status value provided",
                       value = """
                           {
                             "success": false,
                             "message": "Invalid status value",
                             "requestId": "req_123456789",
                             "errors": [
                               "Status must be one of: ACTIVE, INACTIVE, SUSPENDED, BLOCKED"
                             ],
                             "timestamp": "2024-01-15T10:30:00Z"
                           }
                           """
                   ),
                   @ExampleObject(
                       name = "Invalid OTP Error",
                       summary = "Invalid or expired OTP provided",
                       value = """
                           {
                             "success": false,
                             "message": "Invalid or expired OTP",
                             "requestId": "req_123456789",
                             "errors": [
                               "Please request a new OTP and try again"
                             ],
                             "timestamp": "2024-01-15T10:30:00Z"
                           }
                           """
                   )
               }
           )
       ),
       @ApiResponse(
           responseCode = "404",
           description = "User not found",
           content = @Content(
               mediaType = "application/json",
               schema = @Schema(implementation = CustomApiResponse.class),
               examples = @ExampleObject(
                   name = "User Not Found",
                   summary = "User with specified ID not found",
                   value = """
                       {
                         "success": false,
                         "message": "User not found",
                         "requestId": "req_123456789",
                         "errors": [
                           "User with ID usr_987654321 not found"
                         ],
                         "timestamp": "2024-01-15T10:30:00Z"
                       }
                       """
               )
           )
       ),
       @ApiResponse(responseCode = "500", description = "Internal Server Error - Unexpected system error occurred")
   })
    @PutMapping("/{userId}/status")
    public ResponseEntity<CustomApiResponse> updateUserStatus(
            @Parameter(
                name = "userId",
                description = "Unique identifier of the user whose status is to be updated",
                required = true,
                example = "usr_987654321",
                schema = @Schema(type = "string")
            )
            @PathVariable String userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "User status update request including new status and optional OTP for verification. " +
                             "Include the 'otp' field if you have received an OTP code for this operation.",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserStatusRequest.class),
                    examples = {
                        @ExampleObject(
                            name = "Status Update Request",
                            summary = "User status update request without OTP",
                            value = """
                                {
                                  "status": "SUSPENDED",
                                  "reason": "Account suspended due to suspicious activity"
                                }
                                """
                        ),
                        @ExampleObject(
                            name = "Status Update Request with OTP",
                            summary = "User status update request with OTP code",
                            value = """
                                {
                                  "status": "SUSPENDED",
                                  "reason": "Account suspended due to suspicious activity",
                                  "otp": "123456"
                                }
                                """
                        )
                    }
                )
            )
            @Valid @RequestBody UserStatusRequest request) {
        log.info("Received request to update user status for user: {} to status: {}", userId, request.getStatus());
        
        CustomApiResponse response = userService.updateUserStatus(userId, request);
        
        // Determine HTTP status based on response
        HttpStatus status;
        if (response.getSuccess()) {
            status = HttpStatus.OK; // 200 - Status updated successfully
        } else if (response.getMessage() != null && response.getMessage().contains("OTP required")) {
            status = HttpStatus.ACCEPTED; // 202 - OTP required
        } else {
            status = HttpStatus.BAD_REQUEST; // 400 - Validation error or invalid OTP
        }
        
        return ResponseEntity.status(status).body(response);
    }
}
