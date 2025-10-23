package com.boit_droid.wallet.controller;

import com.boit_droid.wallet.dto.request.StatementRequest;
import com.boit_droid.wallet.dto.response.CustomApiResponse;
import com.boit_droid.wallet.dto.response.PagedResponse;
import com.boit_droid.wallet.dto.response.TransactionResponse;
import com.boit_droid.wallet.entity.enums.Status;
import com.boit_droid.wallet.entity.enums.TransactionType;
import com.boit_droid.wallet.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * REST Controller for transaction and statement operations
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Transaction history, statements, and audit trail operations")
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * Generate account statement for a wallet
     */
    @Operation(
        summary = "Generate account statement",
        description = "Generate a comprehensive account statement for a wallet within a specified date range"
    )
   @ApiResponses(value = {
       @ApiResponse(responseCode = "200", description = "Statement generated successfully",
           content = @Content(schema = @Schema(implementation = CustomApiResponse.class))),
       @ApiResponse(responseCode = "400", description = "Invalid request parameters",
           content = @Content(schema = @Schema(implementation = CustomApiResponse.class))),
       @ApiResponse(responseCode = "404", description = "Wallet not found",
           content = @Content(schema = @Schema(implementation = CustomApiResponse.class)))
   })
    @PostMapping("/wallets/{walletId}/statement")
    public ResponseEntity<CustomApiResponse> generateAccountStatement(
            @Parameter(description = "Wallet ID", required = true, example = "wallet-123")
            @PathVariable String walletId,
            @Valid @RequestBody StatementRequest request) {
        
        log.info("Generating account statement for wallet: {}", walletId);
        CustomApiResponse response = transactionService.generateAccountStatement(walletId, request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get transaction history for a wallet with pagination
     */
    @Operation(
        summary = "Get transaction history",
        description = "Retrieve paginated transaction history for a specific wallet with sorting options"
    )
   @ApiResponses(value = {
       @ApiResponse(responseCode = "200", description = "Transaction history retrieved successfully",
           content = @Content(schema = @Schema(implementation = PagedResponse.class))),
       @ApiResponse(responseCode = "404", description = "Wallet not found")
   })
    @GetMapping("/wallets/{walletId}/history")
    public ResponseEntity<PagedResponse<TransactionResponse>> getTransactionHistory(
            @Parameter(description = "Wallet ID", required = true, example = "wallet-123")
            @PathVariable String walletId,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Number of items per page", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Field to sort by", example = "createdAt", 
                schema = @Schema(allowableValues = {"createdAt", "amount", "type", "status"}))
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction", example = "desc",
                schema = @Schema(allowableValues = {"asc", "desc"}))
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Getting transaction history for wallet: {} (page: {}, size: {})", walletId, page, size);
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        PagedResponse<TransactionResponse> response = transactionService.getTransactionHistory(walletId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get transaction history within date range
     */
    @GetMapping("/wallets/{walletId}/history/range")
    public ResponseEntity<PagedResponse<TransactionResponse>> getTransactionHistoryByDateRange(
            @PathVariable String walletId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Getting transaction history for wallet: {} from {} to {}", walletId, startDate, endDate);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PagedResponse<TransactionResponse> response = transactionService.getTransactionHistoryByDateRange(
                walletId, startDate, endDate, pageable);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get transaction details by ID
     */
    @GetMapping("/{transactionId}")
    public ResponseEntity<CustomApiResponse> getTransactionDetails(@PathVariable String transactionId) {
        log.info("Getting transaction details for: {}", transactionId);
        
        CustomApiResponse response = transactionService.getTransactionDetails(transactionId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Search transactions with filters
     */
    @GetMapping("/search")
    public ResponseEntity<PagedResponse<TransactionResponse>> searchTransactions(
            @RequestParam(required = false) String walletId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @RequestParam(required = false) String channel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Searching transactions with filters - wallet: {}, type: {}, status: {}", 
                walletId, type, status);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PagedResponse<TransactionResponse> response = transactionService.searchTransactions(
                walletId, type, status, currency, minAmount, maxAmount, 
                startDate, endDate, channel, pageable);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get transaction audit trail
     */
    @GetMapping("/{transactionId}/audit")
    public ResponseEntity<CustomApiResponse> getTransactionAuditTrail(@PathVariable String transactionId) {
        log.info("Getting audit trail for transaction: {}", transactionId);
        
        CustomApiResponse response = transactionService.getTransactionAuditTrail(transactionId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get comprehensive audit trail for a wallet
     */
    @GetMapping("/wallets/{walletId}/audit")
    public ResponseEntity<PagedResponse<TransactionResponse>> getWalletAuditTrail(
            @PathVariable String walletId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Getting wallet audit trail for: {}", walletId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PagedResponse<TransactionResponse> response = transactionService.getWalletAuditTrail(walletId, pageable);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get transaction statistics for a wallet
     */
    @GetMapping("/wallets/{walletId}/statistics")
    public ResponseEntity<CustomApiResponse> getTransactionStatistics(
            @PathVariable String walletId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate) {
        
        log.info("Getting transaction statistics for wallet: {}", walletId);
        
        CustomApiResponse response = transactionService.getTransactionStatistics(walletId, startDate, endDate);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Verify transaction integrity
     */
    @PostMapping("/{transactionId}/verify")
    public ResponseEntity<CustomApiResponse> verifyTransactionIntegrity(@PathVariable String transactionId) {
        log.info("Verifying transaction integrity for: {}", transactionId);
        
        CustomApiResponse response = transactionService.verifyTransactionIntegrity(transactionId);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get pending transactions for a wallet
     */
    @GetMapping("/wallets/{walletId}/pending")
    public ResponseEntity<List<TransactionResponse>> getPendingTransactions(@PathVariable String walletId) {
        log.info("Getting pending transactions for wallet: {}", walletId);
        
        List<TransactionResponse> response = transactionService.getPendingTransactions(walletId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get failed transactions for a wallet
     */
    @GetMapping("/wallets/{walletId}/failed")
    public ResponseEntity<List<TransactionResponse>> getFailedTransactions(@PathVariable String walletId) {
        log.info("Getting failed transactions for wallet: {}", walletId);
        
        List<TransactionResponse> response = transactionService.getFailedTransactions(walletId);
        return ResponseEntity.ok(response);
    }

    /**
     * Calculate transaction volume for a wallet
     */
    @GetMapping("/wallets/{walletId}/volume")
    public ResponseEntity<CustomApiResponse> calculateTransactionVolume(
            @PathVariable String walletId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate) {
        
        log.info("Calculating transaction volume for wallet: {} from {} to {}", walletId, startDate, endDate);
        
        CustomApiResponse response = transactionService.calculateTransactionVolume(walletId, startDate, endDate);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}