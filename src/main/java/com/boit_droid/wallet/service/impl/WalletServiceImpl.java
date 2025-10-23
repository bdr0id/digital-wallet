package com.boit_droid.wallet.service.impl;

import com.boit_droid.wallet.dto.request.*;
import com.boit_droid.wallet.dto.response.CustomApiResponse;
import com.boit_droid.wallet.dto.response.WalletResponse;
import com.boit_droid.wallet.entity.Transaction;
import com.boit_droid.wallet.entity.User;
import com.boit_droid.wallet.entity.Wallet;
import com.boit_droid.wallet.entity.enums.Status;
import com.boit_droid.wallet.entity.enums.TransactionType;
import com.boit_droid.wallet.repository.TransactionRepository;
import com.boit_droid.wallet.repository.UserRepository;
import com.boit_droid.wallet.repository.WalletRepository;
import com.boit_droid.wallet.service.TransactionService;
import com.boit_droid.wallet.service.TransactionSignatureService;
import com.boit_droid.wallet.service.NotificationService;
import com.boit_droid.wallet.service.OtpService;
import com.boit_droid.wallet.service.WalletService;
import com.boit_droid.wallet.util.QRCodeUtil;
import com.boit_droid.wallet.util.SignatureUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;
    private final TransactionSignatureService transactionSignatureService;
    private final NotificationService notificationService;
    private final OtpService otpService;
    private final SignatureUtil signatureUtil;
    private final QRCodeUtil qrCodeUtil;

    @Override
    @Transactional
    public CustomApiResponse createWallet(String userId, WalletCreationRequest request) {
        String requestId = UUID.randomUUID().toString();
        
        try {
            log.info("Creating wallet for user: {} with request ID: {}", userId, requestId);
            
            // Find user
            User user = userRepository.findByRequestId(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Check if user already has an active wallet
            Optional<Wallet> existingWallets = walletRepository.findByUserAndStatus(user, Status.ACTIVE);
            if (!existingWallets.isEmpty()) {
                return CustomApiResponse.error("User already has an active wallet", requestId,
                    List.of("Only one active wallet per user is allowed"));
            }
            
            // Generate account number
            String accountNumber = generateAccountNumber();
            
            // Create wallet
            Wallet wallet = new Wallet();
            wallet.setUser(user);
            wallet.setRequestId(requestId);
            wallet.setAccountName(request.getAccountName());
            wallet.setAccountNumber(accountNumber);
            wallet.setBalance(BigDecimal.ZERO);
            wallet.setCurrency(request.getCurrency());
            wallet.setStatus(Status.ACTIVE);
            
            // Generate cryptographic elements
            String salt = signatureUtil.generateSalt();
            wallet.setSalt(salt);
            
            String signatureData = signatureUtil.generateWalletIdentificationData(
                requestId, accountNumber, userId);
            wallet.setSignature(signatureUtil.generateSignature(signatureData, salt));
            
            // Set PIN
            String pinSalt = signatureUtil.generateSalt();
            wallet.setPinSalt(pinSalt);
            wallet.setPinHash(signatureUtil.hashPin(request.getPin(), pinSalt));
            wallet.setPinLastUpdated(Instant.now());
            
            // Set default limits
            wallet.setDailyTransactionLimit(new BigDecimal("100000.00"));
            wallet.setMonthlyTransactionLimit(new BigDecimal("1000000.00"));
            wallet.setMaxBalance(new BigDecimal("10000000.00"));
            wallet.setMinBalance(BigDecimal.ZERO);
            
            wallet.setCreatedBy("SYSTEM");
            
            Wallet savedWallet = walletRepository.save(wallet);
            
            WalletResponse response = mapToWalletResponse(savedWallet);
            
            log.info("Wallet created successfully for user: {} with wallet ID: {}", userId, savedWallet.getRequestId());
            return CustomApiResponse.success("Wallet created successfully", requestId, response);
            
        } catch (Exception e) {
            log.error("Error creating wallet for user: {} - {}", userId, e.getMessage(), e);
            return CustomApiResponse.error("Failed to create wallet", requestId,
                List.of(e.getMessage()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CustomApiResponse getWalletBalance(String walletId) {
        String requestId = UUID.randomUUID().toString();
        
        try {
            log.info("Retrieving wallet balance for wallet ID: {}", walletId);
            
            Wallet wallet = walletRepository.findByRequestId(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
            
            // Check if wallet is active
            if (!wallet.isActive()) {
                return CustomApiResponse.error("Wallet is not active", requestId,
                    List.of("Wallet status: " + wallet.getStatus()));
            }
            
            WalletResponse response = mapToWalletResponse(wallet);
            
            log.info("Wallet balance retrieved successfully for wallet ID: {}", walletId);
            return CustomApiResponse.success("Wallet balance retrieved successfully", requestId, response);
            
        } catch (Exception e) {
            log.error("Error retrieving wallet balance for wallet ID: {} - {}", walletId, e.getMessage(), e);
            return CustomApiResponse.error("Failed to retrieve wallet balance", requestId,
                List.of(e.getMessage()));
        }
    }

    @Override
    @Transactional
    public CustomApiResponse enableDisableWallet(String walletId, WalletStatusRequest request) {
        String requestId = UUID.randomUUID().toString();
        
        try {
            log.info("Updating wallet status for wallet ID: {} to status: {}", walletId, request.getStatus());
            
            Wallet wallet = walletRepository.findByRequestId(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
            
            // Verify PIN (supports default '0000' when PIN is not set)
            if (!verifyPinOrHandleFailure(wallet, request.getPin())) {
                return CustomApiResponse.error("Invalid PIN", requestId,
                    List.of("PIN verification failed"));
            }

            // OTP verification (step-up security for status changes)
            String otpPurpose = "STATUS_UPDATE:" + walletId;
            if (request.getOtp() == null || request.getOtp().isBlank()) {
                String code = otpService.requestOtpForWallet(walletId, otpPurpose, wallet.getUser().getId().toString());
                return otpService.createOtpRequiredResponse(requestId, 
                    "OTP required. Code sent to registered channels.", otpPurpose);
            }
            if (!otpService.verifyOtpForWallet(walletId, otpPurpose, request.getOtp())) {
                return CustomApiResponse.error("Invalid or expired OTP", requestId,
                    List.of("Please request a new OTP and try again"));
            }
            
            // Reset PIN attempts on successful verification
            wallet.setPinAttempts(0);
            wallet.setPinLockedUntil(null);
            
            // Update wallet status
            Status newStatus = Status.valueOf(request.getStatus());
            Status oldStatus = wallet.getStatus();
            
            wallet.setStatus(newStatus);
            wallet.setStatusChangedAt(Instant.now());
            wallet.setStatusChangedBy("USER");
            wallet.setStatusChangeReason(request.getReason());
            wallet.setLastModifiedBy("USER");
            
            Wallet savedWallet = walletRepository.save(wallet);
            
            WalletResponse response = mapToWalletResponse(savedWallet);
            
            log.info("Wallet status updated successfully for wallet ID: {} from {} to {}", 
                walletId, oldStatus, newStatus);
            return CustomApiResponse.success("Wallet status updated successfully", requestId, response);
            
        } catch (Exception e) {
            log.error("Error updating wallet status for wallet ID: {} - {}", walletId, e.getMessage(), e);
            return CustomApiResponse.error("Failed to update wallet status", requestId,
                List.of(e.getMessage()));
        }
    }

    @Override
    @Transactional
    public CustomApiResponse deleteWallet(String walletId, WalletDeletionRequest request) {
        String requestId = UUID.randomUUID().toString();
        
        try {
            log.info("Deleting wallet with ID: {}", walletId);
            
            Wallet wallet = walletRepository.findByRequestId(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
            
            // Verify PIN (supports default '0000' when PIN is not set)
            if (!verifyPinOrHandleFailure(wallet, request.getPin())) {
                return CustomApiResponse.error("Invalid PIN", requestId,
                    List.of("PIN verification failed"));
            }
            
            // Check for pending transactions
            List<Transaction> pendingTransactions = transactionRepository
                .findBySenderWalletAndStatusOrderByCreatedAtAsc(wallet, Status.PENDING);
            pendingTransactions.addAll(transactionRepository
                .findByReceiverWalletAndStatusOrderByCreatedAtAsc(wallet, Status.PENDING));
            
            if (!pendingTransactions.isEmpty()) {
                return CustomApiResponse.error("Cannot delete wallet with pending transactions", requestId,
                    List.of("Resolve pending transactions before deletion"));
            }
            
            // Check if wallet has balance
            if (wallet.getBalance().compareTo(BigDecimal.ZERO) > 0) {
                return CustomApiResponse.error("Cannot delete wallet with remaining balance", requestId,
                    List.of("Transfer remaining balance before deletion"));
            }
            
            // OTP verification flow
            String purpose = "WALLET_DELETION:" + walletId;
            
            if (request.getOtp() == null || request.getOtp().trim().isEmpty()) {
                // Generate and send OTP
                log.info("OTP required for wallet deletion: {}", walletId);
                otpService.requestOtpForWallet(walletId, purpose, wallet.getUser().getRequestId());
                
                return otpService.createOtpRequiredResponse(
                    requestId,
                    "OTP required. Code sent to registered channels.",
                    purpose
                );
            }
            
            // Verify OTP
            if (!otpService.verifyOtpForWallet(walletId, purpose, request.getOtp())) {
                log.warn("Invalid OTP provided for wallet deletion: {}", walletId);
                return CustomApiResponse.error("Invalid or expired OTP", requestId,
                    List.of("Please request a new OTP and try again"));
            }
            
            // Soft delete the wallet (handled by @SQLDelete annotation)
            walletRepository.delete(wallet);
            
            log.info("Wallet deleted successfully with ID: {}", walletId);
            
            // Create response data with deletion details
            Map<String, Object> responseData = Map.of(
                "walletId", wallet.getRequestId(),
                "accountNumber", wallet.getAccountNumber(),
                "deletedAt", Instant.now(),
                "finalBalance", wallet.getBalance()
            );
            
            return CustomApiResponse.success("Wallet deleted successfully", requestId, responseData);
            
        } catch (Exception e) {
            log.error("Error deleting wallet with ID: {} - {}", walletId, e.getMessage(), e);
            return CustomApiResponse.error("Failed to delete wallet", requestId,
                List.of(e.getMessage()));
        }
    }

    @Override
    @Transactional
    public CustomApiResponse topUpWallet(String walletId, TopUpRequest request) {
        String requestId = UUID.randomUUID().toString();
        
        try {
            log.info("Processing top-up for wallet ID: {} with amount: {} {}", 
                walletId, request.getAmount(), request.getCurrency());
            
            Wallet wallet = walletRepository.findByRequestId(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
            
            // Check if wallet can transact
            if (!wallet.canTransact()) {
                return CustomApiResponse.error("Wallet cannot perform transactions", requestId,
                    List.of("Wallet status: " + wallet.getStatus() + 
                           (wallet.isPinLocked() ? ", PIN locked until: " + wallet.getPinLockedUntil() : "")));
            }
            
            // Validate currency match
            if (!wallet.getCurrency().equals(request.getCurrency())) {
                return CustomApiResponse.error("Currency mismatch", requestId,
                    List.of("Wallet currency: " + wallet.getCurrency() + 
                           ", Request currency: " + request.getCurrency()));
            }
            
            // Check if M-Pesa transaction ID already exists
            if (transactionRepository.existsByExternalReference(request.getMpesaTransactionId())) {
                return CustomApiResponse.error("Duplicate transaction", requestId,
                    List.of("M-Pesa transaction ID already processed"));
            }
            
            // Simulate M-Pesa integration
            boolean mpesaSuccess = simulateMpesaIntegration(request);
            if (!mpesaSuccess) {
                return CustomApiResponse.error("M-Pesa transaction failed", requestId,
                    List.of("External payment provider rejected the transaction"));
            }
            
            BigDecimal topUpAmount = BigDecimal.valueOf(request.getAmount());
            BigDecimal balanceBefore = wallet.getBalance();
            BigDecimal balanceAfter = balanceBefore.add(topUpAmount);
            
            // Check maximum balance limit
            if (wallet.getMaxBalance() != null && balanceAfter.compareTo(wallet.getMaxBalance()) > 0) {
                return CustomApiResponse.error("Maximum balance limit exceeded", requestId,
                    List.of("Maximum allowed balance: " + wallet.getMaxBalance()));
            }
            
            // Create transaction record
            Transaction transaction = new Transaction();
            transaction.setRequestId(requestId);
            transaction.setSenderWallet(null); // No sender for top-up
            transaction.setReceiverWallet(wallet);
            transaction.setAmount(topUpAmount);
            transaction.setCurrency(request.getCurrency());
            transaction.setType(TransactionType.TOP_UP);
            transaction.setStatus(Status.ACTIVE); // Completed
            transaction.setDescription("Wallet top-up via M-Pesa");
            transaction.setExternalReference(request.getMpesaTransactionId());
            
            // Set balance tracking
            transaction.setReceiverBalanceBefore(balanceBefore);
            transaction.setReceiverBalanceAfter(balanceAfter);
            
            // Set processing details
            transaction.setProcessedAt(Instant.now());
            transaction.setCompletedAt(Instant.now());
            transaction.setProcessedBy("MPESA_INTEGRATION");
            transaction.setChannel("MPESA");
            transaction.setInitiatedBy("USER");
            transaction.setCreatedBy("SYSTEM");
            
            // Generate transaction signature using enhanced signature service
            String transactionSignature = transactionSignatureService.generateTransactionSignature(
                wallet, request.getAmount(), null, "TOP_UP");
            transaction.setTransactionSignature(transactionSignature);
            transaction.setVerified(true);
            transaction.setVerifiedAt(Instant.now());
            transaction.setVerifiedBy("SYSTEM");
            
            // Update wallet balance
            wallet.setBalance(balanceAfter);
            wallet.setLastTransactionAt(Instant.now());
            wallet.setLastModifiedBy("SYSTEM");
            
            // Save transaction and wallet
            Transaction savedTransaction = transactionRepository.save(transaction);
            Wallet savedWallet = walletRepository.save(wallet);
            
            WalletResponse response = mapToWalletResponse(savedWallet);
            
            log.info("Top-up completed successfully for wallet ID: {} with transaction ID: {}", 
                walletId, savedTransaction.getRequestId());
            return CustomApiResponse.success("Wallet top-up completed successfully", requestId, response);
            
        } catch (Exception e) {
            log.error("Error processing top-up for wallet ID: {} - {}", walletId, e.getMessage(), e);
            
            // Create failed transaction record for audit
            try {
                Transaction failedTransaction = new Transaction();
                failedTransaction.setRequestId(requestId);
                failedTransaction.setReceiverWallet(walletRepository.findByRequestId(walletId).orElse(null));
                failedTransaction.setAmount(BigDecimal.valueOf(request.getAmount()));
                failedTransaction.setCurrency(request.getCurrency());
                failedTransaction.setType(TransactionType.TOP_UP);
                failedTransaction.setStatus(Status.CANCELLED);
                failedTransaction.setDescription("Failed wallet top-up via M-Pesa");
                failedTransaction.setExternalReference(request.getMpesaTransactionId());
                failedTransaction.setErrorMessage(e.getMessage());
                failedTransaction.setErrorCode("TOP_UP_FAILED");
                failedTransaction.setCreatedBy("SYSTEM");
                
                transactionRepository.save(failedTransaction);
            } catch (Exception auditException) {
                log.error("Failed to create audit record for failed top-up: {}", auditException.getMessage());
            }
            
            return CustomApiResponse.error("Failed to process wallet top-up", requestId,
                List.of(e.getMessage()));
        }
    }

    @Override
    @Transactional
    public CustomApiResponse transferFunds(String fromWalletId, TransferRequest request) {
        String requestId = UUID.randomUUID().toString();
        
        try {
            log.info("Processing transfer from wallet ID: {} to wallet ID: {} with amount: {} {}", 
                fromWalletId, request.getRecipientWalletId(), request.getAmount(), request.getCurrency());
            
            // Find sender wallet
            Wallet senderWallet = walletRepository.findByRequestId(fromWalletId)
                .orElseThrow(() -> new RuntimeException("Sender wallet not found"));
            
            // Find recipient wallet
            Wallet recipientWallet = walletRepository.findByRequestId(request.getRecipientWalletId())
                .orElseThrow(() -> new RuntimeException("Recipient wallet not found"));
            
            // Validate sender wallet can transact
            if (!senderWallet.canTransact()) {
                return CustomApiResponse.error("Sender wallet cannot perform transactions", requestId,
                    List.of("Wallet status: " + senderWallet.getStatus() + 
                           (senderWallet.isPinLocked() ? ", PIN locked until: " + senderWallet.getPinLockedUntil() : "")));
            }
            
            // Validate recipient wallet is active
            if (!recipientWallet.isActive()) {
                return CustomApiResponse.error("Recipient wallet is not active", requestId,
                    List.of("Recipient wallet status: " + recipientWallet.getStatus()));
            }
            
            // Verify sender PIN (supports default '0000' when PIN is not set)
            if (!verifyPinOrHandleFailure(senderWallet, request.getPin())) {
                return CustomApiResponse.error("Invalid PIN", requestId,
                    List.of("PIN verification failed"));
            }

            // OTP verification (step-up)
            String otpPurpose = "TRANSFER:" + senderWallet.getWalletId() + "->" + request.getRecipientWalletId();
            if (request.getOtp() == null || request.getOtp().isBlank()) {
                String code = otpService.requestOtpForWallet(senderWallet.getWalletId(), otpPurpose, senderWallet.getUser().getId().toString());
                return otpService.createOtpRequiredResponse(requestId, 
                    "OTP required. Code sent to registered channels.", otpPurpose);
            }
            if (!otpService.verifyOtpForWallet(senderWallet.getWalletId(), otpPurpose, request.getOtp())) {
                return CustomApiResponse.error("Invalid or expired OTP", requestId,
                    List.of("Please request a new OTP and try again"));
            }
            
            // Reset PIN attempts on successful verification
            senderWallet.setPinAttempts(0);
            senderWallet.setPinLockedUntil(null);
            
            // Validate currency match
            if (!senderWallet.getCurrency().equals(request.getCurrency()) || 
                !recipientWallet.getCurrency().equals(request.getCurrency())) {
                return CustomApiResponse.error("Currency mismatch", requestId,
                    List.of("All wallets must have the same currency for transfer"));
            }
            
            // Validate transfer amount
            BigDecimal transferAmount = BigDecimal.valueOf(request.getAmount());
            if (transferAmount.compareTo(BigDecimal.ZERO) <= 0) {
                return CustomApiResponse.error("Invalid transfer amount", requestId,
                    List.of("Transfer amount must be greater than zero"));
            }
            
            // Check sufficient balance
            if (senderWallet.getBalance().compareTo(transferAmount) < 0) {
                return CustomApiResponse.error("Insufficient funds", requestId,
                    List.of("Available balance: " + senderWallet.getBalance() + 
                           ", Required: " + transferAmount));
            }
            
            // Check minimum balance constraint
            BigDecimal senderBalanceAfter = senderWallet.getBalance().subtract(transferAmount);
            if (senderWallet.getMinBalance() != null && 
                senderBalanceAfter.compareTo(senderWallet.getMinBalance()) < 0) {
                return CustomApiResponse.error("Minimum balance constraint violated", requestId,
                    List.of("Minimum balance required: " + senderWallet.getMinBalance()));
            }
            
            // Check recipient maximum balance limit
            BigDecimal recipientBalanceAfter = recipientWallet.getBalance().add(transferAmount);
            if (recipientWallet.getMaxBalance() != null && 
                recipientBalanceAfter.compareTo(recipientWallet.getMaxBalance()) > 0) {
                return CustomApiResponse.error("Recipient maximum balance limit exceeded", requestId,
                    List.of("Recipient maximum balance: " + recipientWallet.getMaxBalance()));
            }
            
            // Check daily transaction limit
            if (senderWallet.getDailyTransactionLimit() != null) {
                BigDecimal dailyVolume = calculateDailyTransactionVolume(senderWallet);
                if (dailyVolume.add(transferAmount).compareTo(senderWallet.getDailyTransactionLimit()) > 0) {
                    return CustomApiResponse.error("Daily transaction limit exceeded", requestId,
                        List.of("Daily limit: " + senderWallet.getDailyTransactionLimit() + 
                               ", Current volume: " + dailyVolume));
                }
            }
            
            // Prevent self-transfer
            if (senderWallet.getId().equals(recipientWallet.getId())) {
                return CustomApiResponse.error("Cannot transfer to same wallet", requestId,
                    List.of("Self-transfers are not allowed"));
            }
            
            // Check for duplicate transactions (same amount, wallets, within 5 minutes)
            Instant fiveMinutesAgo = Instant.now().minusSeconds(300);
            List<Transaction> duplicates = transactionRepository.findPotentialDuplicates(
                senderWallet, recipientWallet, transferAmount, 
                fiveMinutesAgo, Instant.now(), -1L);
            
            if (!duplicates.isEmpty()) {
                return CustomApiResponse.error("Potential duplicate transaction detected", requestId,
                    List.of("Similar transaction found within the last 5 minutes"));
            }
            
            // Create transaction record
            Transaction transaction = new Transaction();
            transaction.setRequestId(requestId);
            transaction.setSenderWallet(senderWallet);
            transaction.setReceiverWallet(recipientWallet);
            transaction.setAmount(transferAmount);
            transaction.setCurrency(request.getCurrency());
            transaction.setType(TransactionType.TRANSFER);
            transaction.setStatus(Status.ACTIVE); // Completed
            transaction.setDescription(request.getDescription() != null ? 
                request.getDescription() : "Peer-to-peer transfer");
            
            // Set balance tracking
            transaction.setSenderBalanceBefore(senderWallet.getBalance());
            transaction.setSenderBalanceAfter(senderBalanceAfter);
            transaction.setReceiverBalanceBefore(recipientWallet.getBalance());
            transaction.setReceiverBalanceAfter(recipientBalanceAfter);
            
            // Set processing details
            transaction.setProcessedAt(Instant.now());
            transaction.setCompletedAt(Instant.now());
            transaction.setProcessedBy("WALLET_SERVICE");
            transaction.setChannel("API");
            transaction.setInitiatedBy("USER");
            transaction.setCreatedBy("SYSTEM");
            
            // Generate transaction signature using enhanced signature service
            String transactionSignature = transactionSignatureService.generateTransactionSignature(
                senderWallet, request.getAmount(), request.getRecipientWalletId(), "TRANSFER");
            transaction.setTransactionSignature(transactionSignature);
            transaction.setVerified(true);
            transaction.setVerifiedAt(Instant.now());
            transaction.setVerifiedBy("SYSTEM");
            
            // Update wallet balances
            senderWallet.setBalance(senderBalanceAfter);
            senderWallet.setLastTransactionAt(Instant.now());
            senderWallet.setLastModifiedBy("SYSTEM");
            
            recipientWallet.setBalance(recipientBalanceAfter);
            recipientWallet.setLastTransactionAt(Instant.now());
            recipientWallet.setLastModifiedBy("SYSTEM");
            
            // Save transaction and wallets
            Transaction savedTransaction = transactionRepository.save(transaction);
            Wallet savedSenderWallet = walletRepository.save(senderWallet);
            Wallet savedRecipientWallet = walletRepository.save(recipientWallet);
            
            // Create response with sender wallet details
            WalletResponse response = mapToWalletResponse(savedSenderWallet);
            
            log.info("Transfer completed successfully from wallet ID: {} to wallet ID: {} with transaction ID: {}", 
                fromWalletId, request.getRecipientWalletId(), savedTransaction.getRequestId());
            
            // Send notifications to both parties
            notificationService.sendTransactionNotification(senderWallet.getUser().getId().toString(),
                savedTransaction.getRequestId(), senderWallet.getWalletId(), "DEBIT", request.getAmount(), request.getCurrency());
            notificationService.sendTransactionNotification(recipientWallet.getUser().getId().toString(),
                savedTransaction.getRequestId(), recipientWallet.getWalletId(), "CREDIT", request.getAmount(), request.getCurrency());

            return CustomApiResponse.success("Transfer completed successfully", requestId, response);
            
        } catch (Exception e) {
            log.error("Error processing transfer from wallet ID: {} to wallet ID: {} - {}", 
                fromWalletId, request.getRecipientWalletId(), e.getMessage(), e);
            
            // Create failed transaction record for audit
            try {
                Transaction failedTransaction = new Transaction();
                failedTransaction.setRequestId(requestId);
                failedTransaction.setSenderWallet(walletRepository.findByRequestId(fromWalletId).orElse(null));
                failedTransaction.setReceiverWallet(walletRepository.findByRequestId(request.getRecipientWalletId()).orElse(null));
                failedTransaction.setAmount(BigDecimal.valueOf(request.getAmount()));
                failedTransaction.setCurrency(request.getCurrency());
                failedTransaction.setType(TransactionType.TRANSFER);
                failedTransaction.setStatus(Status.CANCELLED);
                failedTransaction.setDescription("Failed peer-to-peer transfer");
                failedTransaction.setErrorMessage(e.getMessage());
                failedTransaction.setErrorCode("TRANSFER_FAILED");
                failedTransaction.setCreatedBy("SYSTEM");
                
                transactionRepository.save(failedTransaction);
            } catch (Exception auditException) {
                log.error("Failed to create audit record for failed transfer: {}", auditException.getMessage());
            }
            
            return CustomApiResponse.error("Failed to process transfer", requestId,
                List.of(e.getMessage()));
        }
    }

    @Override
    public CustomApiResponse generateQRCode(String walletId) {
        String requestId = UUID.randomUUID().toString();
        
        try {
            log.info("Generating QR code for wallet ID: {}", walletId);
            
            Wallet wallet = walletRepository.findByRequestId(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
            
            if (!wallet.isActive()) {
                return CustomApiResponse.error("Wallet is not active", requestId,
                    List.of("Cannot generate QR code for inactive wallet"));
            }
            
            String qrCode = qrCodeUtil.generateWalletQRCode(
                wallet.getRequestId(), 
                wallet.getAccountNumber(), 
                wallet.getAccountName()
            );
            
            WalletResponse response = mapToWalletResponse(wallet);
            response.setQrCode(qrCode);
            
            log.info("QR code generated successfully for wallet ID: {}", walletId);
            return CustomApiResponse.success("QR code generated successfully", requestId, response);
            
        } catch (Exception e) {
            log.error("Error generating QR code for wallet ID: {} - {}", walletId, e.getMessage(), e);
            return CustomApiResponse.error("Failed to generate QR code", requestId,
                List.of(e.getMessage()));
        }
    }

    @Override
    @Transactional
    public CustomApiResponse updateWalletPIN(String walletId, PINUpdateRequest request) {
        String requestId = UUID.randomUUID().toString();
        
        try {
            log.info("Updating PIN for wallet ID: {}", walletId);
            
            Wallet wallet = walletRepository.findByRequestId(walletId)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
            
            // Verify current PIN (or default '2024' when none is set)
            if (!verifyPinOrHandleFailure(wallet, request.getCurrentPin())) {
                return CustomApiResponse.error("Invalid current PIN", requestId,
                    List.of("Current PIN verification failed"));
            }

            // OTP verification (step-up authentication for PIN changes)
            String otpPurpose = "PIN_UPDATE:" + walletId;
            if (request.getOtp() == null || request.getOtp().isBlank()) {
                String code = otpService.requestOtpForWallet(walletId, otpPurpose, wallet.getUser().getId().toString());
                return otpService.createOtpRequiredResponse(requestId, 
                    "OTP required. Code sent to registered channels.", otpPurpose);
            }
            if (!otpService.verifyOtpForWallet(walletId, otpPurpose, request.getOtp())) {
                return CustomApiResponse.error("Invalid or expired OTP", requestId,
                    List.of("Please request a new OTP and try again"));
            }
            
            // Reset PIN attempts on successful verification
            wallet.setPinAttempts(0);
            wallet.setPinLockedUntil(null);
            
            // Generate new PIN hash
            String newPinSalt = signatureUtil.generateSalt();
            String newPinHash = signatureUtil.hashPin(request.getNewPin(), newPinSalt);
            
            // Update wallet PIN
            wallet.setPinSalt(newPinSalt);
            wallet.setPinHash(newPinHash);
            wallet.setPinLastUpdated(Instant.now());
            wallet.setLastModifiedBy("USER");
            
            Wallet savedWallet = walletRepository.save(wallet);
            
            WalletResponse response = mapToWalletResponse(savedWallet);
            
            log.info("PIN updated successfully for wallet ID: {}", walletId);
            return CustomApiResponse.success("PIN updated successfully", requestId, response);
            
        } catch (Exception e) {
            log.error("Error updating PIN for wallet ID: {} - {}", walletId, e.getMessage(), e);
            return CustomApiResponse.error("Failed to update PIN", requestId,
                List.of(e.getMessage()));
        }
    }

    /**
     * Verify a provided PIN against the wallet's stored PIN, handling attempts/lock and persistence on failure.
     * If the wallet has no PIN set, accepts '2024' as the valid current PIN.
     * @param wallet The wallet being verified
     * @param providedPin The PIN supplied by the user
     * @return true if PIN is valid; false if invalid (after updating attempts/lock and persisting)
     */
    private boolean verifyPinOrHandleFailure(Wallet wallet, String providedPin) {
        try {
            boolean hasExistingPin = wallet.getPinHash() != null && wallet.getPinSalt() != null;
            boolean valid = hasExistingPin
                ? signatureUtil.verifyPin(providedPin, wallet.getPinHash(), wallet.getPinSalt())
                : "2024".equals(providedPin);

            if (!valid) {
                wallet.setPinAttempts(wallet.getPinAttempts() + 1);
                if (wallet.getPinAttempts() >= 3) {
                    wallet.setPinLockedUntil(Instant.now().plusSeconds(3600));
                }
                walletRepository.save(wallet);
                return false;
            }
            // Reset attempts/lock on success (persist with other changes by caller)
            wallet.setPinAttempts(0);
            wallet.setPinLockedUntil(null);
            return true;
        } catch (Exception e) {
            log.error("PIN verification error for wallet {}: {}", wallet.getRequestId(), e.getMessage());
            return false;
        }
    }
    

    @Override
    public CustomApiResponse generateAccountStatement(String walletId, StatementRequest request) {
        log.info("Delegating account statement generation to TransactionService for wallet: {}", walletId);
        return transactionService.generateAccountStatement(walletId, request);
    }

    private String generateAccountNumber() {
        // Generate a unique 12-digit account number
        String timestamp = String.valueOf(System.currentTimeMillis());
        String randomSuffix = String.valueOf((int)(Math.random() * 1000));
        return timestamp.substring(timestamp.length() - 9) + String.format("%03d", Integer.parseInt(randomSuffix));
    }

    /**
     * Calculate daily transaction volume for a wallet
     * @param wallet The wallet to calculate volume for
     * @return Total transaction volume for today
     */
    private BigDecimal calculateDailyTransactionVolume(Wallet wallet) {
        try {
            LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
            BigDecimal volume = transactionRepository.calculateDailyVolumeByWallet(wallet, Instant.from(startOfDay));
            return volume != null ? volume : BigDecimal.ZERO;
        } catch (Exception e) {
            log.warn("Error calculating daily transaction volume for wallet {}: {}", 
                wallet.getRequestId(), e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    /**
     * Simulate M-Pesa integration for wallet top-up
     * In a real implementation, this would call the actual M-Pesa API
     * @param request The top-up request containing M-Pesa details
     * @return true if simulation succeeds, false otherwise
     */
    private boolean simulateMpesaIntegration(TopUpRequest request) {
        try {
            log.info("Simulating M-Pesa integration for transaction ID: {} with phone: {}", 
                request.getMpesaTransactionId(), request.getPhoneNumber());
            
            // Simulate validation checks
            if (request.getMpesaTransactionId() == null || request.getMpesaTransactionId().length() < 10) {
                log.warn("Invalid M-Pesa transaction ID: {}", request.getMpesaTransactionId());
                return false;
            }
            
            if (request.getPhoneNumber() == null || !request.getPhoneNumber().matches("^[0-9]{10,15}$")) {
                log.warn("Invalid phone number: {}", request.getPhoneNumber());
                return false;
            }
            
            // Simulate amount validation
            if (request.getAmount() <= 0 || request.getAmount() > 100000) {
                log.warn("Invalid amount for M-Pesa: {}", request.getAmount());
                return false;
            }
            
            // Simulate network delay
            Thread.sleep(100);
            
            // Simulate 95% success rate (5% failure for testing)
            double successRate = Math.random();
            if (successRate < 0.05) {
                log.warn("M-Pesa simulation failed for transaction ID: {}", request.getMpesaTransactionId());
                return false;
            }
            
            log.info("M-Pesa simulation successful for transaction ID: {}", request.getMpesaTransactionId());
            return true;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("M-Pesa simulation interrupted: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("M-Pesa simulation error: {}", e.getMessage());
            return false;
        }
    }

    private WalletResponse mapToWalletResponse(Wallet wallet) {
        WalletResponse response = new WalletResponse();
        response.setWalletId(wallet.getRequestId());
        response.setAccountNumber(wallet.getAccountNumber());
        response.setAccountName(wallet.getAccountName());
        response.setBalance(wallet.getBalance().doubleValue());
        response.setCurrency(wallet.getCurrency());
        response.setStatus(wallet.getStatus().toString());
        response.setCreatedAt(wallet.getCreatedAt());
        response.setUpdatedAt(wallet.getUpdatedAt());
        return response;
    }
}
