package com.boit_droid.wallet.service;

import com.boit_droid.wallet.dto.request.KYCRequest;
import com.boit_droid.wallet.dto.request.UserRegistrationRequest;
import com.boit_droid.wallet.dto.request.UserStatusRequest;
import com.boit_droid.wallet.dto.response.CustomApiResponse;

public interface UserService {
    
    /**
     * Register a new user and automatically create a wallet
     * @param request User registration request containing user details
     * @return ApiResponse containing user and wallet information
     */
    CustomApiResponse registerUser(UserRegistrationRequest request);
    
    /**
     * Perform KYC verification for a user
     * @param userId User's request ID
     * @param request KYC request containing verification documents and information
     * @return ApiResponse containing KYC verification result
     */
    CustomApiResponse performKYC(String userId, KYCRequest request);
    
    /**
     * Get user profile information
     * @param userId User's request ID
     * @return ApiResponse containing user profile information
     */
    CustomApiResponse getUserProfile(String userId);
    
    /**
     * Update user status
     * @param userId User's request ID
     * @param request User status update request containing new status and optional OTP
     * @return ApiResponse containing updated user information
     */
    CustomApiResponse updateUserStatus(String userId, UserStatusRequest request);
}
