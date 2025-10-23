package com.boit_droid.wallet.controller;

import com.boit_droid.wallet.dto.request.*;
import com.boit_droid.wallet.dto.response.CustomApiResponse;
import com.boit_droid.wallet.dto.response.OtpRequiredResponse;
import com.boit_droid.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
@Tag(name = "Wallets", description = "Wallet management operations including creation, balance management, transfers, and account statements")
public class WalletController {

    private final WalletService walletService;

    @Operation(
        summary = "Create a new wallet",
        description = "Creates a new digital wallet for a user with specified account name, currency, and PIN. The wallet will be assigned a unique wallet ID and account number."
    )
   @ApiResponse(
       responseCode = "201",
       description = "Wallet created successfully",
       content = @Content(
           mediaType = "application/json",
           schema = @Schema(implementation = CustomApiResponse.class),
           examples = @ExampleObject(
               name = "Successful wallet creation",
               value = """
               {
                 "success": true,
                 "message": "Wallet created successfully",
                 "requestId": "req_123456789",
                 "data": {
                   "walletId": "WLT_987654321",
                   "accountNumber": "ACC_001234567890",
                   "accountName": "John Doe Savings",
                   "balance": 0.0,
                   "currency": "KES",
                   "status": "ACTIVE",
                   "createdAt": "2024-01-15T10:30:00Z"
                 },
                 "timestamp": "2024-01-15T10:30:00Z"
               }
               """
           )
       )
   )
   @ApiResponse(
       responseCode = "400",
       description = "Invalid request data or validation errors",
       content = @Content(
           mediaType = "application/json",
           schema = @Schema(implementation = CustomApiResponse.class),
           examples = @ExampleObject(
               name = "Validation error",
               value = """
               {
                 "success": false,
                 "message": "Validation failed",
                 "requestId": "req_123456789",
                 "errors": [
                   "Account name is required",
                   "PIN must be 4 to 6 digits"
                 ],
                 "timestamp": "2024-01-15T10:30:00Z"
               }
               """
           )
       )
   )
    @PostMapping("/create")
    public ResponseEntity<CustomApiResponse> createWallet(
            @Parameter(
                description = "User ID for whom the wallet is being created",
                required = true,
                example = "USR_123456789"
            )
            @RequestParam String userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Wallet creation details including account name, currency, and PIN",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = WalletCreationRequest.class),
                    examples = @ExampleObject(
                        name = "Create wallet request",
                        value = """
                        {
                          "accountName": "John Doe Savings",
                          "currency": "KES",
                          "pin": "1234"
                        }
                        """
                    )
                )
            )
            @Valid @RequestBody WalletCreationRequest request) {
        log.info("Received wallet creation request for user: {}", userId);
        
        CustomApiResponse response = walletService.createWallet(userId, request);
        
        HttpStatus status = response.getSuccess() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }

    @Operation(
        summary = "Get wallet balance",
        description = "Retrieves the current balance and basic information for a specific wallet"
    )
   @ApiResponse(
       responseCode = "200",
       description = "Wallet balance retrieved successfully",
       content = @Content(
           mediaType = "application/json",
           schema = @Schema(implementation = CustomApiResponse.class),
           examples = @ExampleObject(
               name = "Successful balance retrieval",
               value = """
               {
                 "success": true,
                 "message": "Wallet balance retrieved successfully",
                 "requestId": "req_123456789",
                 "data": {
                   "walletId": "WLT_987654321",
                   "accountNumber": "ACC_001234567890",
                   "accountName": "John Doe Savings",
                   "balance": 15750.50,
                   "currency": "KES",
                   "status": "ACTIVE"
                 },
                 "timestamp": "2024-01-15T10:30:00Z"
               }
               """
           )
       )
   )
   @ApiResponse(
       responseCode = "404",
       description = "Wallet not found",
       content = @Content(
           mediaType = "application/json",
           schema = @Schema(implementation = CustomApiResponse.class),
           examples = @ExampleObject(
               name = "Wallet not found",
               value = """
               {
                 "success": false,
                 "message": "Wallet not found",
                 "requestId": "req_123456789",
                 "errors": ["Wallet with ID WLT_987654321 does not exist"],
                 "timestamp": "2024-01-15T10:30:00Z"
               }
               """
           )
       )
   )
    @GetMapping("/{walletId}/balance")
    public ResponseEntity<CustomApiResponse> getWalletBalance(
            @Parameter(
                description = "Unique identifier of the wallet",
                required = true,
                example = "WLT_987654321"
            )
            @PathVariable String walletId) {
        log.info("Received request to get wallet balance for wallet: {}", walletId);
        
        CustomApiResponse response = walletService.getWalletBalance(walletId);
        
        HttpStatus status = response.getSuccess() ? HttpStatus.OK : HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status).body(response);
    }

    @Operation(
        summary = "Top up wallet via M-Pesa simulation",
        description = "Adds funds to a wallet by simulating an M-Pesa transaction. Requires M-Pesa transaction ID and phone number for verification."
    )
   @ApiResponse(
       responseCode = "200",
       description = "Wallet topped up successfully",
       content = @Content(
           mediaType = "application/json",
           schema = @Schema(implementation = CustomApiResponse.class),
           examples = @ExampleObject(
               name = "Successful top-up",
               value = """
               {
                 "success": true,
                 "message": "Wallet topped up successfully",
                 "requestId": "req_123456789",
                 "data": {
                   "walletId": "WLT_987654321",
                   "accountNumber": "ACC_001234567890",
                   "accountName": "John Doe Savings",
                   "balance": 16750.50,
                   "currency": "KES",
                   "status": "ACTIVE",
                   "updatedAt": "2024-01-15T10:35:00Z"
                 },
                 "timestamp": "2024-01-15T10:35:00Z"
               }
               """
           )
       )
   )
   @ApiResponse(
       responseCode = "400",
       description = "Invalid request data or top-up failed",
       content = @Content(
           mediaType = "application/json",
           schema = @Schema(implementation = CustomApiResponse.class),
           examples = @ExampleObject(
               name = "Top-up validation error",
               value = """
               {
                 "success": false,
                 "message": "Top-up validation failed",
                 "requestId": "req_123456789",
                 "errors": [
                   "Amount must be between 1.0 and 1000000.0",
                   "Invalid M-Pesa transaction ID format"
                 ],
                 "timestamp": "2024-01-15T10:35:00Z"
               }
               """
           )
       )
   )
    @PostMapping("/{walletId}/topup")
    public ResponseEntity<CustomApiResponse> topUpWallet(
            @Parameter(
                description = "Unique identifier of the wallet to top up",
                required = true,
                example = "WLT_987654321"
            )
            @PathVariable String walletId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Top-up request details including amount, currency, M-Pesa transaction ID, and phone number",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = TopUpRequest.class),
                    examples = @ExampleObject(
                        name = "Top-up request",
                        value = """
                        {
                          "amount": 1000.00,
                          "currency": "KES",
                          "mpesaTransactionId": "QHX12345678",
                          "phoneNumber": "254712345678"
                        }
                        """
                    )
                )
            )
            @Valid @RequestBody TopUpRequest request) {
        log.info("Received top-up request for wallet: {} with amount: {} {}", 
                walletId, request.getAmount(), request.getCurrency());
        
        CustomApiResponse response = walletService.topUpWallet(walletId, request);
        
        HttpStatus status = response.getSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }

    @Operation(
        summary = "Transfer funds between wallets",
        description = "Transfers funds from one wallet to another. Requires sender's PIN for authorization, OTP verification, and sufficient balance in the sender's wallet. If OTP is not provided, the system will send an OTP and return 202 Accepted status requiring resubmission with OTP."
    )
   @ApiResponse(
       responseCode = "200",
       description = "Transfer completed successfully",
       content = @Content(
           mediaType = "application/json",
           schema = @Schema(implementation = CustomApiResponse.class),
           examples = @ExampleObject(
               name = "Successful transfer",
               value = """
               {
                 "success": true,
                 "message": "Transfer completed successfully",
                 "requestId": "req_123456789",
                 "data": {
                   "transactionId": "TXN_987654321",
                   "senderWallet": {
                     "walletId": "WLT_987654321",
                     "accountNumber": "ACC_001234567890",
                     "balance": 14750.50,
                     "currency": "KES"
                   },
                   "recipientWalletId": "WLT_123456789",
                   "amount": 1000.00,
                   "description": "Payment for services",
                   "status": "COMPLETED"
                 },
                 "timestamp": "2024-01-15T10:40:00Z"
               }
               """
           )
       )
   )
   @ApiResponse(
       responseCode = "202",
       description = "OTP required for transfer completion - request accepted but additional verification needed",
       content = @Content(
           mediaType = "application/json",
           schema = @Schema(implementation = CustomApiResponse.class),
           examples = @ExampleObject(
               name = "OTP required response",
               value = """
               {
                 "success": false,
                 "message": "OTP required. Code sent to registered channels.",
                 "requestId": "9d42b66d-05e7-47fd-a94b-d20e65ad15e2",
                 "locale": null,
                 "salt": "f57634bd-53e9-4d7f-b2d4-8d469d29594d",
                 "signature": "1a82799c-87eb-41db-895e-6d07567419f9",
                 "data": {
                   "purpose": "TRANSFER:WLT_123->WLT_456",
                   "message": "Please provide the 6-digit OTP sent to your registered mobile number",
                   "channels": ["SMS", "EMAIL"],
                   "expirySeconds": 300,
                   "nextStepInstructions": "Resubmit the same request with the 'otp' field included"
                 },
                 "errors": ["Provide the 6-digit OTP to complete the transfer"],
                 "timestamp": "2025-08-20T09:33:35.860038940Z"
               }
               """
           )
       )
   )
   @ApiResponse(
       responseCode = "400",
       description = "Transfer failed due to insufficient funds, invalid PIN, invalid/expired OTP, or validation errors",
       content = @Content(
           mediaType = "application/json",
           schema = @Schema(implementation = CustomApiResponse.class),
           examples = {
               @ExampleObject(
                   name = "Insufficient funds error",
                   value = """
                   {
                     "success": false,
                     "message": "Transfer failed",
                     "requestId": "req_123456789",
                     "errors": [
                       "Insufficient funds. Available balance: 500.00 KES, Required: 1000.00 KES"
                     ],
                     "timestamp": "2024-01-15T10:40:00Z"
                   }
                   """
               ),
               @ExampleObject(
                   name = "Invalid OTP error",
                   value = """
                   {
                     "success": false,
                     "message": "Invalid or expired OTP",
                     "requestId": "req_123456789",
                     "errors": [
                       "Please request a new OTP and try again"
                     ],
                     "timestamp": "2024-01-15T10:40:00Z"
                   }
                   """
               )
           }
       )
   )
    @PostMapping("/{walletId}/transfer")
    public ResponseEntity<CustomApiResponse> transferFunds(
            @Parameter(
                description = "Unique identifier of the sender's wallet",
                required = true,
                example = "WLT_987654321"
            )
            @PathVariable String walletId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Transfer request details including recipient wallet ID, amount, currency, description, sender's PIN, and optional OTP for verification",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = TransferRequest.class),
                    examples = {
                        @ExampleObject(
                            name = "Transfer request without OTP",
                            value = """
                            {
                              "recipientWalletId": "WLT_123456789",
                              "amount": 1000.00,
                              "currency": "KES",
                              "description": "Payment for services",
                              "pin": "1234"
                            }
                            """
                        ),
                        @ExampleObject(
                            name = "Transfer request with OTP",
                            value = """
                            {
                              "recipientWalletId": "WLT_123456789",
                              "amount": 1000.00,
                              "currency": "KES",
                              "description": "Payment for services",
                              "pin": "1234",
                              "otp": "482913"
                            }
                            """
                        )
                    }
                )
            )
            @Valid @RequestBody TransferRequest request) {
        log.info("Received transfer request from wallet: {} to wallet: {} with amount: {} {}", 
                walletId, request.getRecipientWalletId(), request.getAmount(), request.getCurrency());
        
        CustomApiResponse response = walletService.transferFunds(walletId, request);
        
        // Determine HTTP status based on response type and success
        HttpStatus status;
        if (response.getSuccess()) {
            status = HttpStatus.OK;
        } else if (response.getData() instanceof com.boit_droid.wallet.dto.response.OtpRequiredResponse) {
            // OTP required - return 202 Accepted
            status = HttpStatus.ACCEPTED;
        } else {
            // Other errors - return 400 Bad Request
            status = HttpStatus.BAD_REQUEST;
        }
        
        return ResponseEntity.status(status).body(response);
    }

    @Operation(
        summary = "Generate account statement",
        description = "Generates an account statement for a wallet within a specified date range. Supports pagination and multiple output formats (PDF, JSON, CSV)."
    )
   @ApiResponse(
       responseCode = "200",
       description = "Account statement generated successfully",
       content = @Content(
           mediaType = "application/json",
           schema = @Schema(implementation = CustomApiResponse.class),
           examples = @ExampleObject(
               name = "Successful statement generation",
               value = """
               {
                 "success": true,
                 "message": "Account statement generated successfully",
                 "requestId": "req_123456789",
                 "data": {
                   "walletId": "WLT_987654321",
                   "accountNumber": "ACC_001234567890",
                   "statementPeriod": {
                     "startDate": "2024-01-01",
                     "endDate": "2024-01-31"
                   },
                   "transactions": [
                     {
                       "transactionId": "TXN_001",
                       "type": "CREDIT",
                       "amount": 1000.00,
                       "description": "M-Pesa top-up",
                       "timestamp": "2024-01-15T10:30:00Z"
                     }
                   ],
                   "summary": {
                     "openingBalance": 500.00,
                     "closingBalance": 1500.00,
                     "totalCredits": 1000.00,
                     "totalDebits": 0.00
                   },
                   "format": "JSON"
                 },
                 "timestamp": "2024-01-15T10:45:00Z"
               }
               """
           )
       )
   )
   @ApiResponse(
       responseCode = "400",
       description = "Invalid date range or request parameters",
       content = @Content(
           mediaType = "application/json",
           schema = @Schema(implementation = CustomApiResponse.class),
           examples = @ExampleObject(
               name = "Invalid date range",
               value = """
               {
                 "success": false,
                 "message": "Invalid statement request",
                 "requestId": "req_123456789",
                 "errors": [
                   "End date must be after start date",
                   "Date range cannot exceed 12 months"
                 ],
                 "timestamp": "2024-01-15T10:45:00Z"
               }
               """
           )
       )
   )
    @GetMapping("/{walletId}/statement")
    public ResponseEntity<CustomApiResponse> generateAccountStatement(
            @Parameter(
                description = "Unique identifier of the wallet",
                required = true,
                example = "WLT_987654321"
            )
            @PathVariable String walletId,
            @Valid @ModelAttribute StatementRequest request) {
        log.info("Received statement request for wallet: {} from {} to {}", 
                walletId, request.getStartDate(), request.getEndDate());
        
        CustomApiResponse response = walletService.generateAccountStatement(walletId, request);
        
        HttpStatus status = response.getSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }

    @Operation(
        summary = "Generate QR code for wallet",
        description = "Generates a QR code containing wallet information for easy sharing and payments. The QR code can be used by other users to initiate transfers to this wallet."
    )
   @ApiResponse(
       responseCode = "200",
       description = "QR code generated successfully",
       content = @Content(
           mediaType = "application/json",
           schema = @Schema(implementation = CustomApiResponse.class),
           examples = @ExampleObject(
               name = "Successful QR code generation",
               value = """
               {
                 "success": true,
                 "message": "QR code generated successfully",
                 "requestId": "req_123456789",
                 "data": {
                   "walletId": "WLT_987654321",
                   "accountNumber": "ACC_001234567890",
                   "accountName": "John Doe Savings",
                   "qrCode": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAASwAAAEsCAYAAAB5fY51...",
                   "qrCodeText": "wallet:WLT_987654321:ACC_001234567890:John Doe Savings"
                 },
                 "timestamp": "2024-01-15T10:50:00Z"
               }
               """
           )
       )
   )
   @ApiResponse(
       responseCode = "404",
       description = "Wallet not found",
       content = @Content(
           mediaType = "application/json",
           schema = @Schema(implementation = CustomApiResponse.class),
           examples = @ExampleObject(
               name = "Wallet not found",
               value = """
               {
                 "success": false,
                 "message": "Wallet not found",
                 "requestId": "req_123456789",
                 "errors": ["Wallet with ID WLT_987654321 does not exist"],
                 "timestamp": "2024-01-15T10:50:00Z"
               }
               """
           )
       )
   )
    @GetMapping("/{walletId}/qrcode")
    public ResponseEntity<CustomApiResponse> generateQRCode(
            @Parameter(
                description = "Unique identifier of the wallet",
                required = true,
                example = "WLT_987654321"
            )
            @PathVariable String walletId) {
        log.info("Received QR code generation request for wallet: {}", walletId);
        
        CustomApiResponse response = walletService.generateQRCode(walletId);
        
        HttpStatus status = response.getSuccess() ? HttpStatus.OK : HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status).body(response);
    }

    @Operation(
        summary = "Update wallet PIN",
        description = "Updates the PIN for a wallet. Requires the current PIN for verification, OTP verification, and ensures the new PIN is different from the current one. If OTP is not provided, the system will send an OTP and return 202 Accepted status requiring resubmission with OTP."
    )
   @ApiResponse(
       responseCode = "200",
       description = "PIN updated successfully",
       content = @Content(
           mediaType = "application/json",
           schema = @Schema(implementation = CustomApiResponse.class),
           examples = @ExampleObject(
               name = "Successful PIN update",
               value = """
               {
                 "success": true,
                 "message": "PIN updated successfully",
                 "requestId": "req_123456789",
                 "data": {
                   "walletId": "WLT_987654321",
                   "accountNumber": "ACC_001234567890",
                   "accountName": "John Doe Savings",
                   "status": "ACTIVE",
                   "updatedAt": "2024-01-15T10:55:00Z"
                 },
                 "timestamp": "2024-01-15T10:55:00Z"
               }
               """
           )
       )
   )
   @ApiResponse(
       responseCode = "202",
       description = "OTP required for PIN update completion - request accepted but additional verification needed",
       content = @Content(
           mediaType = "application/json",
           schema = @Schema(implementation = CustomApiResponse.class),
           examples = @ExampleObject(
               name = "OTP required response",
               value = """
               {
                 "success": false,
                 "message": "OTP required. Code sent to registered channels.",
                 "requestId": "9d42b66d-05e7-47fd-a94b-d20e65ad15e2",
                 "locale": null,
                 "salt": "f57634bd-53e9-4d7f-b2d4-8d469d29594d",
                 "signature": "1a82799c-87eb-41db-895e-6d07567419f9",
                 "data": {
                   "purpose": "PIN_UPDATE:WLT_987654321",
                   "message": "Please provide the 6-digit OTP sent to your registered mobile number",
                   "channels": ["SMS", "EMAIL"],
                   "expirySeconds": 300,
                   "nextStepInstructions": "Resubmit the same request with the 'otp' field included"
                 },
                 "errors": ["Provide the 6-digit OTP to complete the PIN update"],
                 "timestamp": "2025-08-20T09:33:35.860038940Z"
               }
               """
           )
       )
   )
   @ApiResponse(
       responseCode = "400",
       description = "PIN update failed due to invalid current PIN, invalid/expired OTP, or validation errors",
       content = @Content(
           mediaType = "application/json",
           schema = @Schema(implementation = CustomApiResponse.class),
           examples = {
               @ExampleObject(
                   name = "Invalid current PIN",
                   value = """
                   {
                     "success": false,
                     "message": "PIN update failed",
                     "requestId": "req_123456789",
                     "errors": [
                       "Current PIN is incorrect",
                       "New PIN must be different from current PIN"
                     ],
                     "timestamp": "2024-01-15T10:55:00Z"
                   }
                   """
               ),
               @ExampleObject(
                   name = "Invalid OTP error",
                   value = """
                   {
                     "success": false,
                     "message": "Invalid or expired OTP",
                     "requestId": "req_123456789",
                     "errors": [
                       "Please request a new OTP and try again"
                     ],
                     "timestamp": "2024-01-15T10:55:00Z"
                   }
                   """
               )
           }
       )
   )
    @PutMapping("/{walletId}/pin")
    public ResponseEntity<CustomApiResponse> updateWalletPIN(
            @Parameter(
                description = "Unique identifier of the wallet",
                required = true,
                example = "WLT_987654321"
            )
            @PathVariable String walletId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "PIN update request containing current PIN, new PIN, confirmation, and optional OTP for verification",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = PINUpdateRequest.class),
                    examples = {
                        @ExampleObject(
                            name = "PIN update request without OTP",
                            value = """
                            {
                              "currentPin": "1234",
                              "newPin": "5678",
                              "confirmPin": "5678"
                            }
                            """
                        ),
                        @ExampleObject(
                            name = "PIN update request with OTP",
                            value = """
                            {
                              "currentPin": "1234",
                              "newPin": "5678",
                              "confirmPin": "5678",
                              "otp": "482913"
                            }
                            """
                        )
                    }
                )
            )
            @Valid @RequestBody PINUpdateRequest request) {
        log.info("Received PIN update request for wallet: {}", walletId);
        
        CustomApiResponse response = walletService.updateWalletPIN(walletId, request);
        
        // Determine HTTP status based on response type and success
        HttpStatus status;
        if (response.getSuccess()) {
            status = HttpStatus.OK;
        } else if (response.getData() instanceof com.boit_droid.wallet.dto.response.OtpRequiredResponse) {
            // OTP required - return 202 Accepted
            status = HttpStatus.ACCEPTED;
        } else {
            // Other errors - return 400 Bad Request
            status = HttpStatus.BAD_REQUEST;
        }
        
        return ResponseEntity.status(status).body(response);
    }

    @Operation(
        summary = "Update wallet status",
        description = "Updates the status of a wallet (ACTIVE, INACTIVE, or LOCKED). Requires the wallet PIN for verification, OTP verification, and an optional reason for the status change. If OTP is not provided, the system will send an OTP and return 202 Accepted status requiring resubmission with OTP."
    )
   @ApiResponse(
       responseCode = "200",
       description = "Wallet status updated successfully",
       content = @Content(
           mediaType = "application/json",
           schema = @Schema(implementation = CustomApiResponse.class),
           examples = @ExampleObject(
               name = "Successful status update",
               value = """
               {
                 "success": true,
                 "message": "Wallet status updated successfully",
                 "requestId": "req_123456789",
                 "data": {
                   "walletId": "WLT_987654321",
                   "accountNumber": "ACC_001234567890",
                   "accountName": "John Doe Savings",
                   "status": "INACTIVE",
                   "reason": "Temporary suspension for security review",
                   "updatedAt": "2024-01-15T11:00:00Z"
                 },
                 "timestamp": "2024-01-15T11:00:00Z"
               }
               """
           )
       )
   )
   @ApiResponse(
       responseCode = "202",
       description = "OTP required for status update completion - request accepted but additional verification needed",
       content = @Content(
           mediaType = "application/json",
           schema = @Schema(implementation = CustomApiResponse.class),
           examples = @ExampleObject(
               name = "OTP required response",
               value = """
               {
                 "success": false,
                 "message": "OTP required. Code sent to registered channels.",
                 "requestId": "9d42b66d-05e7-47fd-a94b-d20e65ad15e2",
                 "locale": null,
                 "salt": "f57634bd-53e9-4d7f-b2d4-8d469d29594d",
                 "signature": "1a82799c-87eb-41db-895e-6d07567419f9",
                 "data": {
                   "purpose": "STATUS_UPDATE:WLT_987654321",
                   "message": "Please provide the 6-digit OTP sent to your registered mobile number",
                   "channels": ["SMS", "EMAIL"],
                   "expirySeconds": 300,
                   "nextStepInstructions": "Resubmit the same request with the 'otp' field included"
                 },
                 "errors": ["Provide the 6-digit OTP to complete the status update"],
                 "timestamp": "2025-08-20T09:33:35.860038940Z"
               }
               """
           )
       )
   )
   @ApiResponse(
       responseCode = "400",
       description = "Status update failed due to invalid PIN, invalid/expired OTP, or validation errors",
       content = @Content(
           mediaType = "application/json",
           schema = @Schema(implementation = CustomApiResponse.class),
           examples = {
               @ExampleObject(
                   name = "Invalid PIN or status",
                   value = """
                   {
                     "success": false,
                     "message": "Status update failed",
                     "requestId": "req_123456789",
                     "errors": [
                       "Invalid PIN provided",
                       "Status must be ACTIVE, INACTIVE, or LOCKED"
                     ],
                     "timestamp": "2024-01-15T11:00:00Z"
                   }
                   """
               ),
               @ExampleObject(
                   name = "Invalid OTP error",
                   value = """
                   {
                     "success": false,
                     "message": "Invalid or expired OTP",
                     "requestId": "req_123456789",
                     "errors": [
                       "Please request a new OTP and try again"
                     ],
                     "timestamp": "2024-01-15T11:00:00Z"
                   }
                   """
               )
           }
       )
   )
    @PutMapping("/{walletId}/status")
    public ResponseEntity<CustomApiResponse> updateWalletStatus(
            @Parameter(
                description = "Unique identifier of the wallet",
                required = true,
                example = "WLT_987654321"
            )
            @PathVariable String walletId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Wallet status update request containing new status, PIN, optional reason, and optional OTP for verification",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = WalletStatusRequest.class),
                    examples = {
                        @ExampleObject(
                            name = "Status update request without OTP",
                            value = """
                            {
                              "status": "INACTIVE",
                              "pin": "1234",
                              "reason": "Temporary suspension for security review"
                            }
                            """
                        ),
                        @ExampleObject(
                            name = "Status update request with OTP",
                            value = """
                            {
                              "status": "INACTIVE",
                              "pin": "1234",
                              "reason": "Temporary suspension for security review",
                              "otp": "482913"
                            }
                            """
                        )
                    }
                )
            )
            @Valid @RequestBody WalletStatusRequest request) {
        log.info("Received status update request for wallet: {} to status: {}", 
                walletId, request.getStatus());
        
        CustomApiResponse response = walletService.enableDisableWallet(walletId, request);
        
        // Determine HTTP status based on response type and success
        HttpStatus status;
        if (response.getSuccess()) {
            status = HttpStatus.OK;
        } else if (response.getData() instanceof com.boit_droid.wallet.dto.response.OtpRequiredResponse) {
            // OTP required - return 202 Accepted
            status = HttpStatus.ACCEPTED;
        } else {
            // Other errors - return 400 Bad Request
            status = HttpStatus.BAD_REQUEST;
        }
        
        return ResponseEntity.status(status).body(response);
    }

    @Operation(
        summary = "Delete wallet permanently",
        description = "Permanently deletes a wallet and all associated data. This action cannot be undone. Requires the wallet PIN for verification and OTP confirmation. If OTP is not provided, the system will send an OTP and return 202 Accepted status requiring resubmission with OTP."
    )
   @ApiResponse(
       responseCode = "200",
       description = "Wallet deleted successfully",
       content = @Content(
           mediaType = "application/json",
           schema = @Schema(implementation = CustomApiResponse.class),
           examples = @ExampleObject(
               name = "Successful wallet deletion",
               value = """
               {
                 "success": true,
                 "message": "Wallet deleted successfully",
                 "requestId": "req_123456789",
                 "data": {
                   "walletId": "WLT_987654321",
                   "accountNumber": "ACC_001234567890",
                   "deletedAt": "2024-01-15T11:05:00Z",
                   "finalBalance": 0.0
                 },
                 "timestamp": "2024-01-15T11:05:00Z"
               }
               """
           )
       )
   )
   @ApiResponse(
       responseCode = "202",
       description = "OTP required for wallet deletion completion - request accepted but additional verification needed",
       content = @Content(
           mediaType = "application/json",
           schema = @Schema(implementation = CustomApiResponse.class),
           examples = @ExampleObject(
               name = "OTP required response",
               value = """
               {
                 "success": false,
                 "message": "OTP required. Code sent to registered channels.",
                 "requestId": "9d42b66d-05e7-47fd-a94b-d20e65ad15e2",
                 "locale": null,
                 "salt": "f57634bd-53e9-4d7f-b2d4-8d469d29594d",
                 "signature": "1a82799c-87eb-41db-895e-6d07567419f9",
                 "data": {
                   "purpose": "WALLET_DELETION:WLT_987654321",
                   "message": "Please provide the 6-digit OTP sent to your registered mobile number",
                   "channels": ["SMS", "EMAIL"],
                   "expirySeconds": 300,
                   "nextStepInstructions": "Resubmit the same request with the 'otp' field included"
                 },
                 "errors": ["Provide the 6-digit OTP to complete the wallet deletion"],
                 "timestamp": "2025-08-20T09:33:35.860038940Z"
               }
               """
           )
       )
   )
   @ApiResponse(
       responseCode = "400",
       description = "Wallet deletion failed due to invalid PIN, invalid/expired OTP, non-zero balance, or other constraints",
       content = @Content(
           mediaType = "application/json",
           schema = @Schema(implementation = CustomApiResponse.class),
           examples = {
               @ExampleObject(
                   name = "Deletion failed - non-zero balance",
                   value = """
                   {
                     "success": false,
                     "message": "Wallet deletion failed",
                     "requestId": "req_123456789",
                     "errors": [
                       "Cannot delete wallet with non-zero balance. Current balance: 1500.00 KES",
                       "Please transfer or withdraw all funds before deletion"
                     ],
                     "timestamp": "2024-01-15T11:05:00Z"
                   }
                   """
               ),
               @ExampleObject(
                   name = "Invalid OTP error",
                   value = """
                   {
                     "success": false,
                     "message": "Invalid or expired OTP",
                     "requestId": "req_123456789",
                     "errors": [
                       "Please request a new OTP and try again"
                     ],
                     "timestamp": "2024-01-15T11:05:00Z"
                   }
                   """
               )
           }
       )
   )
   @ApiResponse(
       responseCode = "404",
       description = "Wallet not found",
       content = @Content(
           mediaType = "application/json",
           schema = @Schema(implementation = CustomApiResponse.class),
           examples = @ExampleObject(
               name = "Wallet not found",
               value = """
               {
                 "success": false,
                 "message": "Wallet not found",
                 "requestId": "req_123456789",
                 "errors": ["Wallet with ID WLT_987654321 does not exist"],
                 "timestamp": "2024-01-15T11:05:00Z"
               }
               """
           )
       )
   )
    @DeleteMapping("/{walletId}")
    public ResponseEntity<CustomApiResponse> deleteWallet(
            @Parameter(
                description = "Unique identifier of the wallet to delete",
                required = true,
                example = "WLT_987654321"
            )
            @PathVariable String walletId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Wallet deletion request containing PIN for verification and optional OTP for confirmation",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = WalletDeletionRequest.class),
                    examples = {
                        @ExampleObject(
                            name = "Deletion request without OTP",
                            value = """
                            {
                              "pin": "1234"
                            }
                            """
                        ),
                        @ExampleObject(
                            name = "Deletion request with OTP",
                            value = """
                            {
                              "pin": "1234",
                              "otp": "482913"
                            }
                            """
                        )
                    }
                )
            )
            @Valid @RequestBody WalletDeletionRequest request) {
        log.info("Received wallet deletion request for wallet: {}", walletId);
        
        CustomApiResponse response = walletService.deleteWallet(walletId, request);
        
        // Determine HTTP status based on response type and success
        HttpStatus status;
        if (response.getSuccess()) {
            status = HttpStatus.OK;
        } else if (response.getData() instanceof com.boit_droid.wallet.dto.response.OtpRequiredResponse) {
            // OTP required - return 202 Accepted
            status = HttpStatus.ACCEPTED;
        } else {
            // Other errors - return 400 Bad Request or 404 Not Found
            if (response.getErrors() != null && response.getErrors().stream()
                    .anyMatch(error -> error.toString().contains("not found"))) {
                status = HttpStatus.NOT_FOUND;
            } else {
                status = HttpStatus.BAD_REQUEST;
            }
        }
        
        return ResponseEntity.status(status).body(response);
    }
}
