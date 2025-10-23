package com.boit_droid.wallet.util;

import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for cryptographic operations including PIN management and wallet security
 */
@Component
public class SignatureUtil {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final String SHA256 = "SHA-256";
    private static final int SALT_LENGTH = 32; // 256 bits
    private static final int PIN_MIN_LENGTH = 4;
    private static final int PIN_MAX_LENGTH = 6;
    
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generate a cryptographically secure random salt
     * @return Base64 encoded salt string
     */
    public String generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Generate wallet signature using HMAC-SHA256 with salt
     * @param data The data to sign (wallet ID, user ID, etc.)
     * @param salt The salt to use for signing
     * @return Base64 encoded signature
     * @throws RuntimeException if signature generation fails
     */
    public String generateSignature(String data, String salt) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(salt.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(secretKeySpec);
            
            byte[] signature = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signature);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate signature", e);
        }
    }

    /**
     * Verify wallet signature
     * @param data The original data
     * @param signature The signature to verify
     * @param salt The salt used for signing
     * @return true if signature is valid, false otherwise
     */
    public boolean verifySignature(String data, String signature, String salt) {
        try {
            String expectedSignature = generateSignature(data, salt);
            return MessageDigest.isEqual(
                signature.getBytes(StandardCharsets.UTF_8),
                expectedSignature.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Hash PIN with salt using SHA-256
     * @param pin The PIN to hash
     * @param salt The salt to use
     * @return Base64 encoded hash
     * @throws RuntimeException if hashing fails
     */
    public String hashPin(String pin, String salt) {
        validatePin(pin);
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA256);
            digest.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] hash = digest.digest(pin.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash PIN", e);
        }
    }

    /**
     * Verify PIN against stored hash
     * @param pin The PIN to verify
     * @param storedHash The stored hash to compare against
     * @param salt The salt used for hashing
     * @return true if PIN is valid, false otherwise
     */
    public boolean verifyPin(String pin, String storedHash, String salt) {
        try {
            if (pin == null || storedHash == null || salt == null) {
                return false;
            }
            
            String computedHash = hashPin(pin, salt);
            return MessageDigest.isEqual(
                storedHash.getBytes(StandardCharsets.UTF_8),
                computedHash.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Generate wallet signature data for transaction verification
     * @param walletId The wallet ID
     * @param userId The user ID
     * @param amount The transaction amount
     * @param timestamp The transaction timestamp
     * @return Concatenated data string for signing
     */
    public String generateTransactionSignatureData(String walletId, String userId, String amount, String timestamp) {
        return String.join("|", walletId, userId, amount, timestamp);
    }

    /**
     * Generate wallet identification signature
     * @param walletId The wallet ID
     * @param accountNumber The account number
     * @param userId The user ID
     * @return Concatenated data string for wallet identification
     */
    public String generateWalletIdentificationData(String walletId, String accountNumber, String userId) {
        return String.join("|", walletId, accountNumber, userId);
    }

    /**
     * Validate PIN format and complexity
     * @param pin The PIN to validate
     * @throws IllegalArgumentException if PIN is invalid
     */
    public void validatePin(String pin) {
        if (pin == null || pin.trim().isEmpty()) {
            throw new IllegalArgumentException("PIN cannot be null or empty");
        }
        
        if (pin.length() < PIN_MIN_LENGTH || pin.length() > PIN_MAX_LENGTH) {
            throw new IllegalArgumentException(
                String.format("PIN must be between %d and %d characters", PIN_MIN_LENGTH, PIN_MAX_LENGTH)
            );
        }
        
        if (!pin.matches("\\d+")) {
            throw new IllegalArgumentException("PIN must contain only digits");
        }
        
        // Check for weak PINs (all same digits, sequential)
        if (isWeakPin(pin)) {
            throw new IllegalArgumentException("PIN is too weak. Avoid using repeated or sequential digits");
        }
    }

    /**
     * Check if PIN is considered weak
     * @param pin The PIN to check
     * @return true if PIN is weak, false otherwise
     */
    private boolean isWeakPin(String pin) {
        // Check for all same digits (1111, 0000, etc.)
        if (pin.chars().distinct().count() == 1) {
            return true;
        }
        
        // Check for sequential digits (1234, 4321, etc.)
        boolean isAscending = true;
        boolean isDescending = true;
        
        for (int i = 1; i < pin.length(); i++) {
            int current = Character.getNumericValue(pin.charAt(i));
            int previous = Character.getNumericValue(pin.charAt(i - 1));
            
            if (current != previous + 1) {
                isAscending = false;
            }
            if (current != previous - 1) {
                isDescending = false;
            }
        }
        
        return isAscending || isDescending;
    }

    /**
     * Generate secure random PIN
     * @param length The length of PIN to generate (4-6 digits)
     * @return Generated PIN string
     */
    public String generateSecurePin(int length) {
        if (length < PIN_MIN_LENGTH || length > PIN_MAX_LENGTH) {
            throw new IllegalArgumentException(
                String.format("PIN length must be between %d and %d", PIN_MIN_LENGTH, PIN_MAX_LENGTH)
            );
        }
        
        StringBuilder pin = new StringBuilder();
        for (int i = 0; i < length; i++) {
            pin.append(secureRandom.nextInt(10));
        }
        
        String generatedPin = pin.toString();
        
        // Regenerate if weak PIN is generated
        if (isWeakPin(generatedPin)) {
            return generateSecurePin(length);
        }
        
        return generatedPin;
    }

    /**
     * Generate enhanced signature for OTP operations with additional security context
     * @param userId The user ID
     * @param purpose The OTP purpose
     * @param clientIp The client IP address
     * @param timestamp The timestamp
     * @param salt The salt for signing
     * @return Enhanced signature
     */
    public String generateOtpSignature(String userId, String purpose, String clientIp, String timestamp, String salt) {
        String data = String.join("|", userId, purpose, clientIp, timestamp);
        return generateSignature(data, salt);
    }

    /**
     * Verify OTP signature with security context
     * @param userId The user ID
     * @param purpose The OTP purpose
     * @param clientIp The client IP address
     * @param timestamp The timestamp
     * @param signature The signature to verify
     * @param salt The salt used for signing
     * @return true if signature is valid
     */
    public boolean verifyOtpSignature(String userId, String purpose, String clientIp, String timestamp, 
                                     String signature, String salt) {
        String data = String.join("|", userId, purpose, clientIp, timestamp);
        return verifySignature(data, signature, salt);
    }

    /**
     * Generate request fingerprint for replay attack prevention
     * @param requestData The request data
     * @param timestamp The request timestamp
     * @param nonce A unique nonce value
     * @return SHA-256 hash of the fingerprint
     */
    public String generateRequestFingerprint(String requestData, String timestamp, String nonce) {
        try {
            String fingerprintData = String.join("|", requestData, timestamp, nonce);
            MessageDigest digest = MessageDigest.getInstance(SHA256);
            byte[] hash = digest.digest(fingerprintData.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate request fingerprint", e);
        }
    }

    /**
     * Generate time-based signature that expires after a certain period
     * @param data The data to sign
     * @param salt The salt for signing
     * @param validitySeconds The validity period in seconds
     * @return Time-based signature with embedded expiry
     */
    public String generateTimeBoundSignature(String data, String salt, long validitySeconds) {
        long expiryTime = System.currentTimeMillis() + (validitySeconds * 1000);
        String dataWithExpiry = data + "|" + expiryTime;
        String signature = generateSignature(dataWithExpiry, salt);
        return signature + ":" + expiryTime;
    }

    /**
     * Verify time-based signature and check if it's still valid
     * @param data The original data
     * @param timeBoundSignature The time-bound signature to verify
     * @param salt The salt used for signing
     * @return true if signature is valid and not expired
     */
    public boolean verifyTimeBoundSignature(String data, String timeBoundSignature, String salt) {
        try {
            String[] parts = timeBoundSignature.split(":");
            if (parts.length != 2) {
                return false;
            }
            
            String signature = parts[0];
            long expiryTime = Long.parseLong(parts[1]);
            
            // Check if signature has expired
            if (System.currentTimeMillis() > expiryTime) {
                return false;
            }
            
            String dataWithExpiry = data + "|" + expiryTime;
            return verifySignature(dataWithExpiry, signature, salt);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Generate secure nonce for one-time use operations
     * @return Base64 encoded nonce
     */
    public String generateNonce() {
        byte[] nonce = new byte[16]; // 128 bits
        secureRandom.nextBytes(nonce);
        return Base64.getEncoder().encodeToString(nonce);
    }
}
