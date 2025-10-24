package com.boit_droid.wallet.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * OpenAPI configuration for the Wallet Application API documentation.
 * Provides comprehensive API metadata, tag definitions, global response examples,
 * and common schemas for controller organization.
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Digital Wallet",
        version = "1.0.0",
        description = "Comprehensive digital wallet management system providing user registration, " +
                     "wallet operations, transaction processing, and notification services.",
        contact = @Contact(
            name = "Digital Wallet Development Team",
            email = "collinskboit@gmail.com",
            url = "https://boit-droid.vercel.app/"
        ),
        license = @License(
            name = "MIT License",
            url = "https://opensource.org/licenses/MIT"
        )
    ),
    tags = {
        @Tag(name = "Users", description = "User registration, KYC verification, and profile management"),
        @Tag(name = "Wallets", description = "Wallet operations, balance management, transfers, and QR codes"),
        @Tag(name = "Transactions", description = "Transaction history, statements, and audit trails"),
        @Tag(name = "Notifications", description = "User notifications and messaging system")
    }
)
public class OpenApiConfig {

    /**
     * Customizes the OpenAPI specification with global response examples and common schemas.
     * 
     * @return OpenAPI configuration with enhanced components
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .schemas(createCommonSchemas())
                        .examples(createGlobalExamples())
                        .responses(createGlobalResponses())
                );
    }

    /**
     * Creates common schemas used across multiple endpoints.
     * 
     * @return Map of common schema definitions
     */
    private Map<String, Schema> createCommonSchemas() {
        return Map.of(
                "CustomApiResponse", createApiResponseSchema(),
                "ErrorResponse", createErrorResponseSchema(),
                "PagedResponse", createPagedResponseSchema(),
                "ValidationError", createValidationErrorSchema(),
                "OtpRequiredResponse", createOtpRequiredResponseSchema()
        );
    }

    /**
     * Creates the standard ApiResponse wrapper schema.
     * 
     * @return ApiResponse schema definition
     */
    private Schema createApiResponseSchema() {
        return new Schema<>()
                .type("object")
                .description("Standard API response wrapper")
                .addProperty("success", new Schema<>().type("boolean").description("Operation success status"))
                .addProperty("message", new Schema<>().type("string").description("Human-readable response message"))
                .addProperty("requestId", new Schema<>().type("string").description("Unique request identifier"))
                .addProperty("data", new Schema<>().description("Response payload (varies by endpoint)"))
                .addProperty("errors", new Schema<>().type("array").items(new Schema<>().type("string")).description("Error messages if operation failed"))
                .addProperty("timestamp", new Schema<>().type("string").format("date-time").description("Response timestamp"));
    }

    /**
     * Creates the error response schema.
     * 
     * @return ErrorResponse schema definition
     */
    private Schema createErrorResponseSchema() {
        return new Schema<>()
                .type("object")
                .description("Error response format")
                .addProperty("success", new Schema<>().type("boolean").example(false))
                .addProperty("message", new Schema<>().type("string").description("Error description"))
                .addProperty("requestId", new Schema<>().type("string").description("Request identifier for tracking"))
                .addProperty("errors", new Schema<>().type("array").items(new Schema<>().type("string")).description("Detailed error messages"))
                .addProperty("timestamp", new Schema<>().type("string").format("date-time"));
    }

    /**
     * Creates the paged response schema for paginated endpoints.
     * 
     * @return PagedResponse schema definition
     */
    private Schema createPagedResponseSchema() {
        return new Schema<>()
                .type("object")
                .description("Paginated response wrapper")
                .addProperty("content", new Schema<>().type("array").description("Page content"))
                .addProperty("totalElements", new Schema<>().type("integer").format("int64").description("Total number of elements"))
                .addProperty("totalPages", new Schema<>().type("integer").description("Total number of pages"))
                .addProperty("size", new Schema<>().type("integer").description("Page size"))
                .addProperty("number", new Schema<>().type("integer").description("Current page number"))
                .addProperty("first", new Schema<>().type("boolean").description("Is first page"))
                .addProperty("last", new Schema<>().type("boolean").description("Is last page"));
    }

    /**
     * Creates validation error schema for field validation failures.
     * 
     * @return ValidationError schema definition
     */
    private Schema createValidationErrorSchema() {
        return new Schema<>()
                .type("object")
                .description("Field validation error")
                .addProperty("field", new Schema<>().type("string").description("Field name that failed validation"))
                .addProperty("rejectedValue", new Schema<>().description("Value that was rejected"))
                .addProperty("message", new Schema<>().type("string").description("Validation error message"));
    }

    /**
     * Creates OTP required response schema for operations requiring OTP verification.
     * 
     * @return OtpRequiredResponse schema definition
     */
    private Schema createOtpRequiredResponseSchema() {
        return new Schema<>()
                .type("object")
                .description("Response model for operations that require OTP verification")
                .addProperty("purpose", new Schema<>().type("string").description("Purpose identifier for the OTP request").example("TRANSFER:WLT_123->WLT_456"))
                .addProperty("message", new Schema<>().type("string").description("Human-readable message explaining the OTP requirement").example("Please provide the 6-digit OTP sent to your registered mobile number"))
                .addProperty("channels", new Schema<>().type("array").items(new Schema<>().type("string")).description("List of channels through which the OTP was sent").example(new String[]{"SMS", "EMAIL", "PUSH"}))
                .addProperty("expirySeconds", new Schema<>().type("integer").description("Number of seconds until the OTP expires").example(300))
                .addProperty("nextStepInstructions", new Schema<>().type("string").description("Instructions for the client on how to proceed with OTP verification").example("Resubmit the same request with the 'otp' field included"));
    }

    /**
     * Creates global response examples for common HTTP status codes.
     * 
     * @return Map of global response examples
     */
    private Map<String, Example> createGlobalExamples() {
        return Map.of(
                "SuccessExample", createSuccessExample(),
                "ValidationErrorExample", createValidationErrorExample(),
                "NotFoundExample", createNotFoundExample(),
                "InternalErrorExample", createInternalErrorExample(),
                "InsufficientFundsExample", createInsufficientFundsExample(),
                "InvalidPINExample", createInvalidPINExample(),
                "KYCVerificationExample", createKYCVerificationExample(),
                "WalletStatusExample", createWalletStatusExample(),
                "TransactionFailedExample", createTransactionFailedExample(),
                "OtpRequiredExample", createOtpRequiredExample()
        );
    }

    /**
     * Creates success response example.
     * 
     * @return Success response example
     */
    private Example createSuccessExample() {
        return new Example()
                .summary("Successful operation")
                .description("Standard successful response format")
                .value(Map.of(
                        "success", true,
                        "message", "Operation completed successfully",
                        "requestId", "req_123456789",
                        "data", Map.of("id", 1, "status", "active"),
                        "timestamp", LocalDateTime.now().toString()
                ));
    }

    /**
     * Creates validation error response example.
     * 
     * @return Validation error response example
     */
    private Example createValidationErrorExample() {
        return new Example()
                .summary("Validation error")
                .description("Response when request validation fails")
                .value(Map.of(
                        "success", false,
                        "message", "Validation failed",
                        "requestId", "req_123456789",
                        "errors", new String[]{"Email is required", "Amount must be positive"},
                        "timestamp", LocalDateTime.now().toString()
                ));
    }

    /**
     * Creates not found error response example.
     * 
     * @return Not found response example
     */
    private Example createNotFoundExample() {
        return new Example()
                .summary("Resource not found")
                .description("Response when requested resource doesn't exist")
                .value(Map.of(
                        "success", false,
                        "message", "Resource not found",
                        "requestId", "req_123456789",
                        "errors", new String[]{"User with ID 123 not found"},
                        "timestamp", LocalDateTime.now().toString()
                ));
    }

    /**
     * Creates internal server error response example.
     * 
     * @return Internal server error response example
     */
    private Example createInternalErrorExample() {
        return new Example()
                .summary("Internal server error")
                .description("Response when an unexpected error occurs")
                .value(Map.of(
                        "success", false,
                        "message", "Internal server error",
                        "requestId", "req_123456789",
                        "errors", new String[]{"An unexpected error occurred. Please try again later."},
                        "timestamp", LocalDateTime.now().toString()
                ));
    }

    /**
     * Creates insufficient funds error response example.
     * 
     * @return Insufficient funds response example
     */
    private Example createInsufficientFundsExample() {
        return new Example()
                .summary("Insufficient funds error")
                .description("Response when wallet balance is insufficient for transaction")
                .value(Map.of(
                        "error", "INSUFFICIENT_FUNDS",
                        "message", "Insufficient funds for this transaction",
                        "requestId", "req_123456789",
                        "path", "/api/v1/wallets/transfer",
                        "status", 400,
                        "details", new String[]{"Available balance: 100.00, Required: 150.00"},
                        "timestamp", LocalDateTime.now().toString()
                ));
    }

    /**
     * Creates invalid PIN error response example.
     * 
     * @return Invalid PIN response example
     */
    private Example createInvalidPINExample() {
        return new Example()
                .summary("Invalid PIN error")
                .description("Response when PIN validation fails")
                .value(Map.of(
                        "error", "INVALID_PIN",
                        "message", "Invalid PIN provided",
                        "requestId", "req_123456789",
                        "path", "/api/v1/wallets/update-pin",
                        "status", 400,
                        "details", new String[]{"PIN validation failed. 2 attempts remaining."},
                        "timestamp", LocalDateTime.now().toString()
                ));
    }

    /**
     * Creates KYC verification error response example.
     * 
     * @return KYC verification response example
     */
    private Example createKYCVerificationExample() {
        return new Example()
                .summary("KYC verification failed")
                .description("Response when KYC verification process fails")
                .value(Map.of(
                        "error", "KYC_VERIFICATION_FAILED",
                        "message", "KYC verification failed",
                        "requestId", "req_123456789",
                        "path", "/api/v1/users/kyc",
                        "status", 400,
                        "details", new String[]{"Document verification failed: Invalid document format"},
                        "timestamp", LocalDateTime.now().toString()
                ));
    }

    /**
     * Creates wallet status error response example.
     * 
     * @return Wallet status error response example
     */
    private Example createWalletStatusExample() {
        return new Example()
                .summary("Wallet status error")
                .description("Response when wallet status prevents operation")
                .value(Map.of(
                        "error", "WALLET_STATUS_ERROR",
                        "message", "Wallet status does not allow this operation",
                        "requestId", "req_123456789",
                        "path", "/api/v1/wallets/transfer",
                        "status", 400,
                        "details", new String[]{"Wallet is currently SUSPENDED. Contact support."},
                        "timestamp", LocalDateTime.now().toString()
                ));
    }

    /**
     * Creates transaction failed error response example.
     * 
     * @return Transaction failed response example
     */
    private Example createTransactionFailedExample() {
        return new Example()
                .summary("Transaction processing failed")
                .description("Response when transaction processing encounters an error")
                .value(Map.of(
                        "error", "TRANSACTION_FAILED",
                        "message", "Transaction processing failed",
                        "requestId", "req_123456789",
                        "path", "/api/v1/wallets/transfer",
                        "status", 422,
                        "details", new String[]{"Transaction could not be processed due to system constraints"},
                        "timestamp", LocalDateTime.now().toString()
                ));
    }

    /**
     * Creates OTP required response example.
     * 
     * @return OTP required response example
     */
    private Example createOtpRequiredExample() {
        return new Example()
                .summary("OTP verification required")
                .description("Response when operation requires OTP verification")
                .value(Map.of(
                        "success", false,
                        "message", "OTP required. Code sent to registered channels.",
                        "requestId", "9d42b66d-05e7-47fd-a94b-d20e65ad15e2",
                        "salt", "f57634bd-53e9-4d7f-b2d4-8d469d29594d",
                        "signature", "1a82799c-87eb-41db-895e-6d07567419f9",
                        "data", Map.of(
                                "purpose", "TRANSFER:WLT_123->WLT_456",
                                "message", "Please provide the 6-digit OTP sent to your registered mobile number",
                                "channels", new String[]{"SMS", "EMAIL"},
                                "expirySeconds", 300,
                                "nextStepInstructions", "Resubmit the same request with the 'otp' field included"
                        ),
                        "errors", new String[]{"Provide the 6-digit OTP to complete the operation"},
                        "timestamp", LocalDateTime.now().toString()
                ));
    }

    /**
     * Creates global response definitions for common HTTP status codes.
     * 
     * @return Map of global response definitions
     */
    private Map<String, ApiResponse> createGlobalResponses() {
        return Map.of(
                "BadRequest", createBadRequestResponse(),
                "Unauthorized", createUnauthorizedResponse(),
                "Forbidden", createForbiddenResponse(),
                "NotFound", createNotFoundResponse(),
                "MethodNotAllowed", createMethodNotAllowedResponse(),
                "UnprocessableEntity", createUnprocessableEntityResponse(),
                "InternalServerError", createInternalServerErrorResponse()
        );
    }

    /**
     * Creates bad request (400) response definition.
     * 
     * @return Bad request response definition
     */
    private ApiResponse createBadRequestResponse() {
        return new ApiResponse()
                .description("Bad Request - Validation failed or invalid input")
                .content(new Content()
                        .addMediaType("application/json", new MediaType()
                                .schema(new Schema<>().$ref("#/components/schemas/CustomApiResponse"))
                                .example(Map.of(
                                        "success", false,
                                        "message", "Validation failed",
                                        "requestId", "req_123456789",
                                        "errors", new String[]{"Invalid input parameters"},
                                        "timestamp", LocalDateTime.now().toString()
                                ))
                        )
                );
    }

    /**
     * Creates not found (404) response definition.
     * 
     * @return Not found response definition
     */
    private ApiResponse createNotFoundResponse() {
        return new ApiResponse()
                .description("Not Found - Requested resource does not exist")
                .content(new Content()
                        .addMediaType("application/json", new MediaType()
                                .schema(new Schema<>().$ref("#/components/schemas/CustomApiResponse"))
                                .example(Map.of(
                                        "success", false,
                                        "message", "Resource not found",
                                        "requestId", "req_123456789",
                                        "errors", new String[]{"Requested resource does not exist"},
                                        "timestamp", LocalDateTime.now().toString()
                                ))
                        )
                );
    }

    /**
     * Creates unauthorized (401) response definition.
     * 
     * @return Unauthorized response definition
     */
    private ApiResponse createUnauthorizedResponse() {
        return new ApiResponse()
                .description("Unauthorized - Authentication required")
                .content(new Content()
                        .addMediaType("application/json", new MediaType()
                                .schema(new Schema<>().$ref("#/components/schemas/CustomApiResponse"))
                                .example(Map.of(
                                        "success", false,
                                        "message", "Authentication required",
                                        "requestId", "req_123456789",
                                        "errors", new String[]{"Authentication required"},
                                        "timestamp", LocalDateTime.now().toString()
                                ))
                        )
                );
    }

    /**
     * Creates forbidden (403) response definition.
     * 
     * @return Forbidden response definition
     */
    private ApiResponse createForbiddenResponse() {
        return new ApiResponse()
                .description("Forbidden - Insufficient permissions")
                .content(new Content()
                        .addMediaType("application/json", new MediaType()
                                .schema(new Schema<>().$ref("#/components/schemas/CustomApiResponse"))
                                .example(Map.of(
                                        "success", false,
                                        "message", "Security validation failed",
                                        "requestId", "req_123456789",
                                        "errors", new String[]{"Insufficient permissions"},
                                        "timestamp", LocalDateTime.now().toString()
                                ))
                        )
                );
    }

    /**
     * Creates method not allowed (405) response definition.
     * 
     * @return Method not allowed response definition
     */
    private ApiResponse createMethodNotAllowedResponse() {
        return new ApiResponse()
                .description("Method Not Allowed - HTTP method not supported")
                .content(new Content()
                        .addMediaType("application/json", new MediaType()
                                .schema(new Schema<>().$ref("#/components/schemas/CustomApiResponse"))
                                .example(Map.of(
                                        "success", false,
                                        "message", "HTTP method not supported for this endpoint",
                                        "requestId", "req_123456789",
                                        "errors", new String[]{"HTTP method 'DELETE' is not supported for this endpoint"},
                                        "timestamp", LocalDateTime.now().toString()
                                ))
                        )
                );
    }

    /**
     * Creates unprocessable entity (422) response definition.
     * 
     * @return Unprocessable entity response definition
     */
    private ApiResponse createUnprocessableEntityResponse() {
        return new ApiResponse()
                .description("Unprocessable Entity - Business logic validation failed")
                .content(new Content()
                        .addMediaType("application/json", new MediaType()
                                .schema(new Schema<>().$ref("#/components/schemas/CustomApiResponse"))
                                .example(Map.of(
                                        "success", false,
                                        "message", "Transaction processing failed",
                                        "requestId", "req_123456789",
                                        "errors", new String[]{"Insufficient funds: Available 100.00, Required 150.00"},
                                        "timestamp", LocalDateTime.now().toString()
                                ))
                        )
                );
    }

    /**
     * Creates internal server error (500) response definition.
     * 
     * @return Internal server error response definition
     */
    private ApiResponse createInternalServerErrorResponse() {
        return new ApiResponse()
                .description("Internal Server Error - Unexpected system error")
                .content(new Content()
                        .addMediaType("application/json", new MediaType()
                                .schema(new Schema<>().$ref("#/components/schemas/CustomApiResponse"))
                                .example(Map.of(
                                        "success", false,
                                        "message", "An unexpected error occurred",
                                        "requestId", "req_123456789",
                                        "errors", new String[]{"Internal server error occurred"},
                                        "timestamp", LocalDateTime.now().toString()
                                ))
                        )
                );
    }
}