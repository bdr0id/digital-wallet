package com.boit_droid.wallet.service;

import com.boit_droid.wallet.dto.response.CustomApiResponse;
import com.boit_droid.wallet.dto.response.OtpRequiredResponse;

public interface OtpService {

	String requestOtpForWallet(String walletId, String purpose, String userId);

	boolean verifyOtpForWallet(String walletId, String purpose, String otpCode);

	/**
	 * Request OTP for user-based operations (KYC, status updates, etc.)
	 * @param userId The user ID requesting the OTP
	 * @param purpose The purpose of the OTP request
	 * @return The generated OTP code
	 */
	String requestOtpForUser(String userId, String purpose);

	/**
	 * Verify OTP for user-based operations
	 * @param userId The user ID verifying the OTP
	 * @param purpose The purpose of the OTP verification
	 * @param otpCode The OTP code to verify
	 * @return true if OTP is valid, false otherwise
	 */
	boolean verifyOtpForUser(String userId, String purpose, String otpCode);

	/**
	 * Create a standardized OTP-required response
	 * @param requestId The request ID for tracking
	 * @param message The message to display to the user
	 * @param purpose The purpose of the OTP request
	 * @return CustomApiResponse with OtpRequiredResponse data
	 */
	CustomApiResponse<OtpRequiredResponse> createOtpRequiredResponse(String requestId, String message, String purpose);

	/**
	 * Request OTP with rate limiting and security checks
	 * @param userId The user ID requesting the OTP
	 * @param purpose The purpose of the OTP request
	 * @param clientIp The client IP address for security tracking
	 * @param requestId The request ID for audit logging
	 * @return The generated OTP code
	 * @throws SecurityException if rate limit exceeded or suspicious activity detected
	 */
	String requestOtpWithSecurity(String userId, String purpose, String clientIp, String requestId);

	/**
	 * Verify OTP with enhanced security and audit logging
	 * @param userId The user ID verifying the OTP
	 * @param purpose The purpose of the OTP verification
	 * @param otpCode The OTP code to verify
	 * @param clientIp The client IP address for security tracking
	 * @param requestId The request ID for audit logging
	 * @return true if OTP is valid, false otherwise
	 */
	boolean verifyOtpWithSecurity(String userId, String purpose, String otpCode, String clientIp, String requestId);

	/**
	 * Check if user has exceeded OTP generation rate limit
	 * @param userId The user ID to check
	 * @param clientIp The client IP address
	 * @return true if rate limit exceeded
	 */
	boolean isRateLimited(String userId, String clientIp);

	/**
	 * Check for suspicious OTP patterns
	 * @param userId The user ID to check
	 * @param clientIp The client IP address
	 * @return true if suspicious activity detected
	 */
	boolean isSuspiciousActivity(String userId, String clientIp);
}






