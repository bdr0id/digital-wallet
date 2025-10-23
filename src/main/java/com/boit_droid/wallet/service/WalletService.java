package com.boit_droid.wallet.service;

import com.boit_droid.wallet.dto.request.*;
import com.boit_droid.wallet.dto.response.CustomApiResponse;

public interface WalletService {
    
    // Wallet creation and management
    CustomApiResponse createWallet(String userId, WalletCreationRequest request);
    
    // Wallet balance and account management (Task 5.1)
    CustomApiResponse getWalletBalance(String walletId);
    CustomApiResponse enableDisableWallet(String walletId, WalletStatusRequest request);
    CustomApiResponse deleteWallet(String walletId, WalletDeletionRequest request);
    
    // Wallet top-up functionality (Task 5.2)
    CustomApiResponse topUpWallet(String walletId, TopUpRequest request);
    
    // Peer-to-peer transfer functionality (Task 5.3)
    CustomApiResponse transferFunds(String fromWalletId, TransferRequest request);
    
    // Additional wallet operations
    CustomApiResponse generateQRCode(String walletId);
    CustomApiResponse updateWalletPIN(String walletId, PINUpdateRequest request);
    CustomApiResponse generateAccountStatement(String walletId, StatementRequest request);
}
