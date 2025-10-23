package com.boit_droid.wallet.exception;

import com.boit_droid.wallet.dto.response.ErrorResponse;
import com.boit_droid.wallet.util.RequestIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler for consistent error responses across the application
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Handle wallet-specific exceptions
     */
    @ExceptionHandler(WalletException.class)
    public ResponseEntity<ErrorResponse> handleWalletException(WalletException ex, WebRequest request) {
        String requestId = RequestIdGenerator.getCurrentRequestId();
        HttpStatus status = determineHttpStatus(ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
            ex.getErrorCode(),
            ex.getMessage(),
            requestId,
            getPath(request),
            status.value()
        );
        
        // Add specific details for certain exceptions
        if (ex instanceof ValidationException) {
            errorResponse.setDetails(((ValidationException) ex).getValidationErrors());
        }
        
        logger.error("Wallet exception [{}]: {} - Request ID: {}", 
                    ex.getErrorCode(), ex.getMessage(), requestId, ex);
        
        return new ResponseEntity<>(errorResponse, status);
    }
    
    /**
     * Handle validation errors from @Valid annotations
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        String requestId = RequestIdGenerator.getCurrentRequestId();
        List<String> errors = new ArrayList<>();
        
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
            "VALIDATION_ERROR",
            "Request validation failed",
            requestId,
            getPath(request),
            HttpStatus.BAD_REQUEST.value()
        );
        errorResponse.setDetails(errors);
        
        logger.warn("Validation error - Request ID: {} - Errors: {}", requestId, errors);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle constraint violation exceptions
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex, WebRequest request) {
        
        String requestId = RequestIdGenerator.getCurrentRequestId();
        List<String> errors = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "CONSTRAINT_VIOLATION",
            "Constraint validation failed",
            requestId,
            getPath(request),
            HttpStatus.BAD_REQUEST.value()
        );
        errorResponse.setDetails(errors);
        
        logger.warn("Constraint violation - Request ID: {} - Errors: {}", requestId, errors);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle bind exceptions
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException ex, WebRequest request) {
        String requestId = RequestIdGenerator.getCurrentRequestId();
        List<String> errors = new ArrayList<>();
        
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }
        
        ErrorResponse errorResponse = new ErrorResponse(
            "BINDING_ERROR",
            "Request binding failed",
            requestId,
            getPath(request),
            HttpStatus.BAD_REQUEST.value()
        );
        errorResponse.setDetails(errors);
        
        logger.warn("Binding error - Request ID: {} - Errors: {}", requestId, errors);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle missing request parameters
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameterException(
            MissingServletRequestParameterException ex, WebRequest request) {
        
        String requestId = RequestIdGenerator.getCurrentRequestId();
        String message = String.format("Missing required parameter: %s", ex.getParameterName());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "MISSING_PARAMETER",
            message,
            requestId,
            getPath(request),
            HttpStatus.BAD_REQUEST.value()
        );
        
        logger.warn("Missing parameter error - Request ID: {} - Parameter: {}", 
                   requestId, ex.getParameterName());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle method argument type mismatch
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex, WebRequest request) {
        
        String requestId = RequestIdGenerator.getCurrentRequestId();
        String message = String.format("Invalid value for parameter '%s': %s", 
                                      ex.getName(), ex.getValue());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "TYPE_MISMATCH",
            message,
            requestId,
            getPath(request),
            HttpStatus.BAD_REQUEST.value()
        );
        
        logger.warn("Type mismatch error - Request ID: {} - Parameter: {} - Value: {}", 
                   requestId, ex.getName(), ex.getValue());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle HTTP message not readable (malformed JSON)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, WebRequest request) {
        
        String requestId = RequestIdGenerator.getCurrentRequestId();
        
        ErrorResponse errorResponse = new ErrorResponse(
            "MALFORMED_REQUEST",
            "Request body is malformed or unreadable",
            requestId,
            getPath(request),
            HttpStatus.BAD_REQUEST.value()
        );
        
        logger.warn("Malformed request - Request ID: {} - Error: {}", requestId, ex.getMessage());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle unsupported HTTP methods
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex, WebRequest request) {
        
        String requestId = RequestIdGenerator.getCurrentRequestId();
        String message = String.format("HTTP method '%s' is not supported for this endpoint", 
                                      ex.getMethod());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "METHOD_NOT_SUPPORTED",
            message,
            requestId,
            getPath(request),
            HttpStatus.METHOD_NOT_ALLOWED.value()
        );
        
        logger.warn("Method not supported - Request ID: {} - Method: {}", requestId, ex.getMethod());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.METHOD_NOT_ALLOWED);
    }
    
    /**
     * Handle security-related exceptions
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponse> handleSecurityException(SecurityException ex, WebRequest request) {
        String requestId = RequestIdGenerator.getCurrentRequestId();
        
        ErrorResponse errorResponse = new ErrorResponse(
            "SECURITY_ERROR",
            "Security validation failed",
            requestId,
            getPath(request),
            HttpStatus.FORBIDDEN.value()
        );
        
        logger.error("Security error - Request ID: {} - Error: {}", requestId, ex.getMessage(), ex);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }
    
    /**
     * Handle illegal argument exceptions (often from validation)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        String requestId = RequestIdGenerator.getCurrentRequestId();
        
        ErrorResponse errorResponse = new ErrorResponse(
            "INVALID_ARGUMENT",
            ex.getMessage(),
            requestId,
            getPath(request),
            HttpStatus.BAD_REQUEST.value()
        );
        
        logger.warn("Invalid argument - Request ID: {} - Error: {}", requestId, ex.getMessage());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle runtime exceptions related to cryptographic operations
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex, WebRequest request) {
        String requestId = RequestIdGenerator.getCurrentRequestId();
        
        // Check if it's a cryptographic or signature-related error
        if (ex.getMessage() != null && 
            (ex.getMessage().contains("signature") || 
             ex.getMessage().contains("PIN") || 
             ex.getMessage().contains("crypto"))) {
            
            ErrorResponse errorResponse = new ErrorResponse(
                "SECURITY_OPERATION_FAILED",
                "Security operation failed",
                requestId,
                getPath(request),
                HttpStatus.BAD_REQUEST.value()
            );
            
            logger.error("Security operation failed - Request ID: {} - Error: {}", 
                        requestId, ex.getMessage(), ex);
            
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
        
        // For other runtime exceptions, treat as internal server error
        ErrorResponse errorResponse = new ErrorResponse(
            "INTERNAL_SERVER_ERROR",
            "An unexpected error occurred",
            requestId,
            getPath(request),
            HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        
        logger.error("Runtime error - Request ID: {} - Error: {}", requestId, ex.getMessage(), ex);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        String requestId = RequestIdGenerator.getCurrentRequestId();
        
        ErrorResponse errorResponse = new ErrorResponse(
            "INTERNAL_SERVER_ERROR",
            "An unexpected error occurred",
            requestId,
            getPath(request),
            HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        
        logger.error("Unexpected error - Request ID: {} - Error: {}", requestId, ex.getMessage(), ex);
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Determine appropriate HTTP status based on exception type
     */
    private HttpStatus determineHttpStatus(WalletException ex) {
        return switch (ex.getErrorCode()) {
            case "USER_NOT_FOUND", "WALLET_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "INSUFFICIENT_FUNDS", "INVALID_PIN", "KYC_VERIFICATION_FAILED", 
                 "WALLET_STATUS_ERROR", "VALIDATION_ERROR" -> HttpStatus.BAD_REQUEST;
            case "TRANSACTION_FAILED" -> HttpStatus.UNPROCESSABLE_ENTITY;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
    
    /**
     * Extract path from web request
     */
    private String getPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}