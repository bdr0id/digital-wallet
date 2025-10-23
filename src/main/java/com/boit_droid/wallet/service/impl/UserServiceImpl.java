package com.boit_droid.wallet.service.impl;

import com.boit_droid.wallet.dto.request.KYCRequest;
import com.boit_droid.wallet.dto.request.UserRegistrationRequest;
import com.boit_droid.wallet.dto.request.UserStatusRequest;
import com.boit_droid.wallet.dto.response.CustomApiResponse;
import com.boit_droid.wallet.dto.response.UserResponse;
import com.boit_droid.wallet.dto.response.WalletResponse;
import com.boit_droid.wallet.entity.User;
import com.boit_droid.wallet.entity.Wallet;
import com.boit_droid.wallet.entity.enums.Status;
import com.boit_droid.wallet.repository.UserRepository;
import com.boit_droid.wallet.repository.WalletRepository;
import com.boit_droid.wallet.service.DatabaseConstraintService;
import com.boit_droid.wallet.service.EntityValidationService;
import com.boit_droid.wallet.service.OtpService;
import com.boit_droid.wallet.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;




@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final EntityValidationService entityValidationService;
    private final DatabaseConstraintService databaseConstraintService;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public CustomApiResponse getUserProfile(String userId) {
        String requestId = UUID.randomUUID().toString();

        try {
            log.info("Retrieving user profile for user: {}", userId);

            // Find user by request ID
            User user = userRepository.findByRequestId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

            // Map to response
            UserResponse userResponse = mapToUserResponse(user);

            return CustomApiResponse.success(
                    "User profile retrieved successfully",
                    requestId,
                    userResponse
            );

        } catch (IllegalArgumentException e) {
            log.error("Failed to retrieve user profile: {}", e.getMessage());
            return CustomApiResponse.error(
                    "Failed to retrieve user profile: " + e.getMessage(),
                    requestId,
                    Arrays.asList(e.getMessage())
            );
        } catch (Exception e) {
            log.error("Unexpected error retrieving user profile: {}", e.getMessage(), e);
            return CustomApiResponse.error(
                    "Failed to retrieve user profile due to system error",
                    requestId,
                    Arrays.asList("An unexpected error occurred. Please try again later.")
            );
        }
    }

    @Override
    @Transactional
    public CustomApiResponse updateUserStatus(String userId, UserStatusRequest request) {
        String requestId = UUID.randomUUID().toString();

        try {
            log.info("Updating user status for user: {} to status: {}", userId, request.getStatus());

            // Find user by request ID
            User user = userRepository.findByRequestId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

            // Validate status
            Status newStatus = validateAndParseStatus(request.getStatus());

            // Check if OTP is provided
            if (request.getOtp() == null || request.getOtp().trim().isEmpty()) {
                // No OTP provided - generate and send OTP
                log.info("No OTP provided for user status update, generating OTP for user: {}", userId);

                String purpose = "USER_STATUS_UPDATE:" + userId;
                otpService.requestOtpForUser(userId, purpose);

                // Return 202 Accepted with OTP required response
                return otpService.createOtpRequiredResponse(
                        requestId,
                        "OTP required. Code sent to registered channels.",
                        purpose
                );
            }

            // OTP provided - verify it
            String purpose = "USER_STATUS_UPDATE:" + userId;
            boolean isOtpValid = otpService.verifyOtpForUser(userId, purpose, request.getOtp().trim());

            if (!isOtpValid) {
                log.warn("Invalid OTP provided for user status update for user: {}", userId);
                return CustomApiResponse.error(
                        "Invalid or expired OTP",
                        requestId,
                        Arrays.asList("Please request a new OTP and try again")
                );
            }

            log.info("OTP verified successfully for user status update for user: {}", userId);

            // Update user status
            Status oldStatus = user.getStatus();
            user.setStatus(newStatus);
            user.setLastModifiedBy("SYSTEM_ADMIN");

            // Save updated user
            User updatedUser = userRepository.save(user);
            log.info("User status updated successfully from {} to {} for user: {}",
                    oldStatus, newStatus, userId);

            // Prepare response data with additional information
            Map<String, Object> responseData = new HashMap<>();
            UserResponse userResponse = mapToUserResponse(updatedUser);
            responseData.put("userId", userResponse.getUserId());
            responseData.put("firstName", userResponse.getFirstName());
            responseData.put("lastName", userResponse.getLastName());
            responseData.put("email", userResponse.getEmail());
            responseData.put("mobile", userResponse.getMobile());
            responseData.put("status", newStatus.name());
            responseData.put("previousStatus", oldStatus.name());
            responseData.put("updatedAt", updatedUser.getUpdatedAt());
            if (request.getReason() != null && !request.getReason().trim().isEmpty()) {
                responseData.put("reason", request.getReason());
            }

            return CustomApiResponse.success(
                    String.format("User status updated successfully from %s to %s", oldStatus, newStatus),
                    requestId,
                    responseData
            );

        } catch (IllegalArgumentException e) {
            log.error("Failed to update user status: {}", e.getMessage());
            return CustomApiResponse.error(
                    "Failed to update user status: " + e.getMessage(),
                    requestId,
                    Arrays.asList(e.getMessage())
            );
        } catch (Exception e) {
            log.error("Unexpected error updating user status: {}", e.getMessage(), e);
            return CustomApiResponse.error(
                    "Failed to update user status due to system error",
                    requestId,
                    Arrays.asList("An unexpected error occurred. Please try again later.")
            );
        }
    }

    @Override
    @Transactional
    public CustomApiResponse  performKYC(String userId, KYCRequest request) {
        String requestId = UUID.randomUUID().toString();

        try {
            log.info("Starting KYC verification process for user: {}", userId);

            // Find user by request ID
            User user = userRepository.findByRequestId(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

            // Validate KYC request (basic validation)
            validateKYCRequest(request, user);

            // Check if OTP is provided
            if (request.getOtp() == null || request.getOtp().trim().isEmpty()) {
                // No OTP provided - generate and send OTP
                log.info("No OTP provided for KYC verification, generating OTP for user: {}", userId);

                String purpose = "KYC_VERIFICATION:" + userId;
                otpService.requestOtpForUser(userId, purpose);

                // Return 202 Accepted with OTP required response
                return otpService.createOtpRequiredResponse(
                        requestId,
                        "OTP required. Code sent to registered channels.",
                        purpose
                );
            }

            // OTP provided - verify it
            String purpose = "KYC_VERIFICATION:" + userId;
            boolean isOtpValid = otpService.verifyOtpForUser(userId, purpose, request.getOtp().trim());

            if (!isOtpValid) {
                log.warn("Invalid OTP provided for KYC verification for user: {}", userId);
                return CustomApiResponse.error(
                        "Invalid or expired OTP",
                        requestId,
                        Arrays.asList("Please request a new OTP and try again")
                );
            }

            log.info("OTP verified successfully for KYC verification for user: {}", userId);

            // Proceed with KYC verification process
            KYCVerificationResult verificationResult = simulateKYCVerification(request, user);

            // Update user KYC status based on verification result
            updateUserKYCStatus(user, verificationResult, request);

            // Save updated user
            User updatedUser = userRepository.save(user);
            log.info("KYC verification completed for user: {} with status: {}", userId, updatedUser.getKycStatus());

            // Prepare response data
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("user", mapToUserResponse(updatedUser));
            responseData.put("kycResult", createKYCResultResponse(verificationResult));

            String message = verificationResult.isVerified()
                    ? "KYC verification successful. Your account is now fully verified."
                    : "KYC verification failed. Please review the feedback and resubmit.";

            return CustomApiResponse.success(message, requestId, responseData);

        } catch (IllegalArgumentException e) {
            log.error("KYC verification failed due to validation error: {}", e.getMessage());
            return CustomApiResponse.error(
                    "KYC verification failed: " + e.getMessage(),
                    requestId,
                    Arrays.asList(e.getMessage())
            );
        } catch (Exception e) {
            log.error("Unexpected error during KYC verification: {}", e.getMessage(), e);
            return CustomApiResponse.error(
                    "KYC verification failed due to system error",
                    requestId,
                    Arrays.asList("An unexpected error occurred. Please try again later.")
            );
        }
    }

    @Override
    @Transactional
    public CustomApiResponse registerUser(UserRegistrationRequest request) {
        String requestId = UUID.randomUUID().toString();

        try {
            log.info("Starting user registration process for mobile: {}", request.getMobile());

            validateRegistrationRequest(request);

            String purpose = "USER_REGISTRATION:" + request.getMobile();

            if (request.getOtp() == null || request.getOtp().trim().isEmpty()) {
                log.info("No OTP provided for registration, generating OTP for mobile: {}", request.getMobile());

                otpService.requestOtpForUser(request.getMobile(), purpose);

                return otpService.createOtpRequiredResponse(
                        requestId,
                        "OTP required. Code sent to your registered mobile/email.",
                        purpose
                );
            }

            boolean isOtpValid = otpService.verifyOtpForUser(request.getMobile(), purpose, request.getOtp().trim());
            if (!isOtpValid) {
                log.warn("Invalid OTP provided during registration for mobile: {}", request.getMobile());
                return CustomApiResponse.error(
                        "Invalid or expired OTP",
                        requestId,
                        Arrays.asList("Please request a new OTP and try again")
                );
            }

            log.info("OTP verified successfully for registration of mobile: {}", request.getMobile());

            checkForDuplicateUser(request);

            User user = createUserEntity(request, requestId);
            entityValidationService.validateUser(user);
            databaseConstraintService.validateUserConstraints(user);

            User savedUser = userRepository.save(user);
            log.info("User created successfully with ID: {}", savedUser.getRequestId());

            Wallet wallet = createWalletForUser(savedUser);
            entityValidationService.validateWallet(wallet);
            databaseConstraintService.validateWalletConstraints(wallet);

            Wallet savedWallet = walletRepository.save(wallet);
            log.info("Wallet created successfully for user: {}", savedUser.getRequestId());

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("user", mapToUserResponse(savedUser));
            responseData.put("wallet", mapToWalletResponse(savedWallet));

            return CustomApiResponse.success(
                    "User registration successful. Wallet created automatically.",
                    requestId,
                    responseData
            );

        } catch (IllegalArgumentException e) {
            log.error("User registration failed due to validation error: {}", e.getMessage());
            return CustomApiResponse.error(
                    "Registration failed: " + e.getMessage(),
                    requestId,
                    Arrays.asList(e.getMessage())
            );
        } catch (Exception e) {
            log.error("Unexpected error during user registration: {}", e.getMessage(), e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw e;
        }
    }


    private Status validateAndParseStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }

        try {
            return Status.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status + ". Valid statuses are: ACTIVE, INACTIVE, SUSPENDED, DELETED");
        }
    }

    private void validateRegistrationRequest(UserRegistrationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Registration request cannot be null");
        }

        // Additional business validation beyond bean validation
        if (request.getPassword().length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }

        if (request.getMobile().startsWith("0")) {
            throw new IllegalArgumentException("Mobile number should not start with 0");
        }

        // Validate locale format if provided
        if (request.getLocale() != null && !request.getLocale().matches("^[a-z]{2}(_[A-Z]{2})?$")) {
            throw new IllegalArgumentException("Invalid locale format. Use format like 'en' or 'en_US'");
        }
    }

    private void checkForDuplicateUser(UserRegistrationRequest request) {
        List<String> duplicateErrors = new ArrayList<>();

        if (userRepository.existsByMobile(request.getMobile())) {
            duplicateErrors.add("Mobile number already registered: " + request.getMobile());
        }

        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            duplicateErrors.add("Email already registered: " + request.getEmail());
        }

        if (userRepository.existsByIdNumber(request.getIdNumber())) {
            duplicateErrors.add("ID number already registered: " + request.getIdNumber());
        }

        if (!duplicateErrors.isEmpty()) {
            throw new IllegalArgumentException(String.join(", ", duplicateErrors));
        }
    }

    private User createUserEntity(UserRegistrationRequest request, String requestId) {
        User user = new User();
        user.setRequestId(requestId);
        user.setFirstName(request.getFirstName());
        user.setMiddleName(request.getMiddleName());
        user.setLastName(request.getLastName());
        user.setGender(request.getGender());
        user.setCountryCode(request.getCountryCode());
        user.setMobile(request.getMobile());
        user.setIdNumber(request.getIdNumber());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setLocale(request.getLocale() != null ? request.getLocale() : "en");
        user.setStatus(Status.ACTIVE);
        user.setKycStatus("NOT_STARTED");
        user.setCreatedBy("SYSTEM");

        return user;
    }

    private Wallet createWalletForUser(User user) {
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setRequestId(UUID.randomUUID().toString());
        wallet.setAccountName(user.getFirstName() + " " + user.getLastName() + " Wallet");
        wallet.setAccountNumber(generateAccountNumber());
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setCurrency("KES");
        wallet.setStatus(Status.PENDING);
        wallet.setSalt(generateSalt());
        wallet.setSignature(generateSignature());
        wallet.setPinAttempts(0);
        wallet.setCreatedBy("SYSTEM");

        return wallet;
    }

    private String generateAccountNumber() {
        // Generate a 12-digit account number
        StringBuilder accountNumber = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            accountNumber.append(secureRandom.nextInt(10));
        }

        // Ensure uniqueness
        String generatedNumber = accountNumber.toString();
        while (walletRepository.existsByAccountNumber(generatedNumber)) {
            accountNumber = new StringBuilder();
            for (int i = 0; i < 12; i++) {
                accountNumber.append(secureRandom.nextInt(10));
            }
            generatedNumber = accountNumber.toString();
        }

        return generatedNumber;
    }

    private String generateSalt() {
        byte[] salt = new byte[32];
        secureRandom.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    private String generateSignature() {
        byte[] signature = new byte[64];
        secureRandom.nextBytes(signature);
        return Base64.getEncoder().encodeToString(signature);
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setUserId(user.getRequestId());
        response.setFirstName(user.getFirstName());
        response.setMiddleName(user.getMiddleName());
        response.setLastName(user.getLastName());
        response.setGender(user.getGender());
        response.setCountryCode(user.getCountryCode());
        response.setMobile(user.getMobile());
        response.setIdNumber(user.getIdNumber());
        response.setEmail(user.getEmail());
        response.setLocale(user.getLocale());
        response.setStatus(user.getStatus().name());
        response.setKycStatus(user.getKycStatus());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());

        return response;
    }

    private WalletResponse mapToWalletResponse(Wallet wallet) {
        WalletResponse response = new WalletResponse();
        response.setWalletId(wallet.getRequestId());
        response.setAccountNumber(wallet.getAccountNumber());
        response.setAccountName(wallet.getAccountName());
        response.setBalance(wallet.getBalance().doubleValue());
        response.setCurrency(wallet.getCurrency());
        response.setStatus(wallet.getStatus().name());
        response.setCreatedAt(wallet.getCreatedAt());
        response.setUpdatedAt(wallet.getUpdatedAt());

        return response;
    }

    // KYC Verification Helper Methods

    private void validateKYCRequest(KYCRequest request, User user) {
        if (request == null) {
            throw new IllegalArgumentException("KYC request cannot be null");
        }

        // Check if user is eligible for KYC
        if (!"ACTIVE".equals(user.getStatus().name())) {
            throw new IllegalArgumentException("User account must be active to perform KYC verification");
        }

        if ("VERIFIED".equals(user.getKycStatus())) {
            throw new IllegalArgumentException("User is already KYC verified");
        }

        // Validate required fields
        if (request.getDocumentType() == null || request.getDocumentType().trim().isEmpty()) {
            throw new IllegalArgumentException("Document type is required for KYC verification");
        }

        if (request.getDocumentNumber() == null || request.getDocumentNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Document number is required for KYC verification");
        }

        if (request.getDocumentImageBase64() == null || request.getDocumentImageBase64().trim().isEmpty()) {
            throw new IllegalArgumentException("Document image is required for KYC verification");
        }

        if (request.getSelfieImageBase64() == null || request.getSelfieImageBase64().trim().isEmpty()) {
            throw new IllegalArgumentException("Selfie image is required for KYC verification");
        }

        // Validate document type
        if (!isValidDocumentType(request.getDocumentType())) {
            throw new IllegalArgumentException("Invalid document type. Supported types: ID_CARD, PASSPORT, DRIVING_LICENSE");
        }

        // Validate document number format based on type
        validateDocumentNumber(request.getDocumentType(), request.getDocumentNumber());

        // Validate base64 images
//        validateBase64Image(request.getDocumentImageBase64(), "Document image");
//        validateBase64Image(request.getSelfieImageBase64(), "Selfie image");
    }

    private boolean isValidDocumentType(String documentType) {
        return Arrays.asList("ID_CARD", "PASSPORT", "DRIVING_LICENSE").contains(documentType.toUpperCase());
    }

    private void validateDocumentNumber(String documentType, String documentNumber) {
        switch (documentType.toUpperCase()) {
            case "ID_CARD":
                if (!documentNumber.matches("^[0-9]{8}$")) {
                    throw new IllegalArgumentException("ID Card number must be 8 digits");
                }
                break;
            case "PASSPORT":
                if (!documentNumber.matches("^[A-Z]{1,2}[0-9]{6,8}$")) {
                    throw new IllegalArgumentException("Invalid passport number format");
                }
                break;
            case "DRIVING_LICENSE":
                if (!documentNumber.matches("^[A-Z0-9]{8,15}$")) {
                    throw new IllegalArgumentException("Invalid driving license number format");
                }
                break;
        }
    }

    private void validateBase64Image(String base64Image, String imageType) {
        try {
            // Basic validation - check if it's valid base64
            Base64.getDecoder().decode(base64Image);

            // Check minimum size (should be at least 1KB for a valid image)
            if (base64Image.length() < 1000) {
                throw new IllegalArgumentException(imageType + " appears to be too small or invalid");
            }

            // Check maximum size (limit to 5MB)
            if (base64Image.length() > 5 * 1024 * 1024) {
                throw new IllegalArgumentException(imageType + " is too large. Maximum size is 5MB");
            }

        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Illegal base64 character")) {
                throw new IllegalArgumentException(imageType + " must be a valid base64 encoded image");
            }
            throw e;
        }
    }

    private KYCVerificationResult simulateKYCVerification(KYCRequest request, User user) {
        log.info("Simulating KYC verification for user: {}", user.getRequestId());

        // Simulate processing time
        try {
            Thread.sleep(1000); // 1 second delay to simulate processing
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Simulate verification logic with some randomness and business rules
        boolean isVerified = performSimulatedVerification(request, user);

        KYCVerificationResult result = new KYCVerificationResult();
        result.setVerified(isVerified);
        result.setVerificationId(UUID.randomUUID().toString());
        result.setProcessedAt(Instant.now());

        if (isVerified) {
            result.setMessage("KYC verification successful. All documents verified.");
            result.setScore(95 + secureRandom.nextInt(5)); // Score between 95-99
        } else {
            result.setMessage("KYC verification failed. Please check document quality and resubmit.");
            result.setScore(30 + secureRandom.nextInt(40)); // Score between 30-69
            result.setRejectionReasons(generateRejectionReasons());
        }

        return result;
    }

    private boolean performSimulatedVerification(KYCRequest request, User user) {
        // Simulate various verification checks

        // 1. Document quality check (90% pass rate)
        if (secureRandom.nextInt(100) < 10) {
            return false; // Document quality issues
        }

        // 2. Face matching check (95% pass rate)
        if (secureRandom.nextInt(100) < 5) {
            return false; // Face matching failed
        }

        // 3. Document authenticity check (85% pass rate)
        if (secureRandom.nextInt(100) < 15) {
            return false; // Document authenticity issues
        }

        // 4. Information consistency check
        if (!isInformationConsistent(request, user)) {
            return false; // Information mismatch
        }

        // 5. Blacklist check (99% pass rate)
        if (secureRandom.nextInt(100) < 1) {
            return false; // User in blacklist
        }

        return true; // All checks passed
    }

    private boolean isInformationConsistent(KYCRequest request, User user) {
        // Check if document number matches user's ID number (for ID cards)
        if ("ID_CARD".equals(request.getDocumentType().toUpperCase())) {
            return request.getDocumentNumber().equals(user.getIdNumber());
        }

        // For other document types, assume consistency (in real implementation, 
        // you would extract name from document and compare)
        return true;
    }

    private List<String> generateRejectionReasons() {
        List<String> possibleReasons = Arrays.asList(
                "Document image quality is too low",
                "Document appears to be damaged or altered",
                "Selfie image quality is insufficient",
                "Face in selfie does not match document photo",
                "Document information does not match user profile",
                "Document appears to be expired",
                "Unable to verify document authenticity"
        );

        // Return 1-3 random reasons
        List<String> reasons = new ArrayList<>();
        int numReasons = 1 + secureRandom.nextInt(3);

        Collections.shuffle(possibleReasons);
        for (int i = 0; i < numReasons && i < possibleReasons.size(); i++) {
            reasons.add(possibleReasons.get(i));
        }

        return reasons;
    }

    private void updateUserKYCStatus(User user, KYCVerificationResult result, KYCRequest request) {
        if (result.isVerified()) {
            user.setKycStatus("VERIFIED");
            user.setKycVerifiedAt(Instant.now());
            user.setKycVerifiedBy("SYSTEM_AUTO");
            user.setKycRejectionReason(null);
            // Activate user's pending wallets upon successful KYC verification
            try {
                List<Wallet> userWallets = walletRepository.findByUser(user);
                if (userWallets != null && !userWallets.isEmpty()) {
                    boolean anyUpdated = false;
                    for (Wallet wallet : userWallets) {
                        if (wallet.getStatus() == Status.PENDING) {
                            wallet.setStatus(Status.ACTIVE);
                            wallet.setStatusChangedAt(Instant.now());
                            wallet.setStatusChangedBy("SYSTEM_KYC");
                            wallet.setStatusChangeReason("Activated on successful KYC verification");
                            wallet.setLastModifiedBy("SYSTEM_KYC");
                            anyUpdated = true;
                        }
                    }
                    if (anyUpdated) {
                        walletRepository.saveAll(userWallets);
                        log.info("Activated pending wallets for user {} due to successful KYC", user.getRequestId());
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to activate wallets after KYC for user {}: {}", user.getRequestId(), e.getMessage());
            }
        } else {
            user.setKycStatus("REJECTED");
            user.setKycVerifiedAt(null);
            user.setKycVerifiedBy(null);
            user.setKycRejectionReason(String.join("; ", result.getRejectionReasons()));
        }

        // Update document information
        user.setKycDocumentType(request.getDocumentType());
        user.setKycDocumentNumber(request.getDocumentNumber());
        user.setLastModifiedBy("SYSTEM_KYC");
    }

    private Map<String, Object> createKYCResultResponse(KYCVerificationResult result) {
        Map<String, Object> kycResult = new HashMap<>();
        kycResult.put("verificationId", result.getVerificationId());
        kycResult.put("verified", result.isVerified());
        kycResult.put("score", result.getScore());
        kycResult.put("message", result.getMessage());
        kycResult.put("processedAt", result.getProcessedAt());

        if (!result.isVerified() && result.getRejectionReasons() != null) {
            kycResult.put("rejectionReasons", result.getRejectionReasons());
        }

        return kycResult;
    }

    // Inner class for KYC verification result
    private static class KYCVerificationResult {
        private boolean verified;
        private String verificationId;
        private String message;
        private int score;
        private Instant processedAt;
        private List<String> rejectionReasons;

        // Getters and setters
        public boolean isVerified() { return verified; }
        public void setVerified(boolean verified) { this.verified = verified; }

        public String getVerificationId() { return verificationId; }
        public void setVerificationId(String verificationId) { this.verificationId = verificationId; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }

        public Instant getProcessedAt() { return processedAt; }
        public void setProcessedAt(Instant processedAt) { this.processedAt = processedAt; }

        public List<String> getRejectionReasons() { return rejectionReasons; }
        public void setRejectionReasons(List<String> rejectionReasons) { this.rejectionReasons = rejectionReasons; }
    }
}
