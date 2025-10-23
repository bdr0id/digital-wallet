package com.boit_droid.wallet.entity.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Types of transactions supported by the wallet system")
public enum TransactionType {
    @Schema(description = "Money deposited into a wallet")
    DEPOSIT,
    
    @Schema(description = "Money withdrawn from a wallet")
    WITHDRAWAL,
    
    @Schema(description = "Money transferred between wallets")
    TRANSFER,
    
    @Schema(description = "Wallet balance top-up via M-Pesa or other payment methods")
    TOP_UP,
    
    @Schema(description = "Refund of a previous transaction")
    REFUND,
    
    @Schema(description = "Reversal of a transaction due to error or dispute")
    REVERSAL,
    
    @Schema(description = "Service fee charged for a transaction")
    FEE,
    
    @Schema(description = "Interest earned on wallet balance")
    INTEREST,
    
    @Schema(description = "Bonus amount credited to wallet")
    BONUS,
    
    @Schema(description = "Penalty amount debited from wallet")
    PENALTY
}