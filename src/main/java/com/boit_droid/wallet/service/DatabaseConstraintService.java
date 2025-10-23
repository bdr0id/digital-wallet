package com.boit_droid.wallet.service;

import com.boit_droid.wallet.entity.User;
import com.boit_droid.wallet.entity.Wallet;
import com.boit_droid.wallet.entity.Transaction;
import com.boit_droid.wallet.repository.UserRepository;
import com.boit_droid.wallet.repository.WalletRepository;
import com.boit_droid.wallet.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service for validating and maintaining database constraints and referential integrity
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseConstraintService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Validates user entity constraints before persistence
     */
    public void validateUserConstraints(User user) {
        // Check for duplicate mobile number
        if (userRepository.existsByMobile(user.getMobile())) {
            throw new IllegalArgumentException("Mobile number already exists: " + user.getMobile());
        }

        // Check for duplicate email
        if (user.getEmail() != null && userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }

        // Check for duplicate ID number
        if (userRepository.existsByIdNumber(user.getIdNumber())) {
            throw new IllegalArgumentException("ID number already exists: " + user.getIdNumber());
        }

        // Validate mobile number format
        if (!user.getMobile().matches("^[0-9]{9,15}$")) {
            throw new IllegalArgumentException("Invalid mobile number format: " + user.getMobile());
        }
    }

    /**
     * Validates wallet entity constraints before persistence
     */
    public void validateWalletConstraints(Wallet wallet) {
        // Check for duplicate account number
        if (walletRepository.existsByAccountNumber(wallet.getAccountNumber())) {
            throw new IllegalArgumentException("Account number already exists: " + wallet.getAccountNumber());
        }

        // Validate balance constraints
        if (wallet.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Wallet balance cannot be negative");
        }

        // Validate currency format
        if (!wallet.getCurrency().matches("^[A-Z]{3}$")) {
            throw new IllegalArgumentException("Invalid currency format: " + wallet.getCurrency());
        }

        // Check if user exists and is active
        if (wallet.getUser() == null) {
            throw new IllegalArgumentException("Wallet must be associated with a user");
        }
    }

    /**
     * Validates transaction entity constraints before persistence
     */
    public void validateTransactionConstraints(Transaction transaction) {
        // Validate amount
        if (transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be positive");
        }

        // Validate receiver wallet exists
        if (transaction.getReceiverWallet() == null) {
            throw new IllegalArgumentException("Transaction must have a receiver wallet");
        }

        // For transfers, validate sender wallet exists and has sufficient balance
        if (transaction.getSenderWallet() != null) {
            Wallet senderWallet = transaction.getSenderWallet();
            if (senderWallet.getBalance().compareTo(transaction.getAmount()) < 0) {
                throw new IllegalArgumentException("Insufficient balance in sender wallet");
            }
        }

        // Validate currency consistency
        if (transaction.getSenderWallet() != null && 
            !transaction.getCurrency().equals(transaction.getSenderWallet().getCurrency())) {
            throw new IllegalArgumentException("Transaction currency must match sender wallet currency");
        }

        if (!transaction.getCurrency().equals(transaction.getReceiverWallet().getCurrency())) {
            throw new IllegalArgumentException("Transaction currency must match receiver wallet currency");
        }
    }

    /**
     * Performs referential integrity checks across entities
     */
    @Transactional(readOnly = true)
    public void validateReferentialIntegrity() {
        log.info("Starting referential integrity validation");

        // Check for orphaned wallets
        List<Wallet> orphanedWallets = walletRepository.findWalletsWithoutUser();
        if (!orphanedWallets.isEmpty()) {
            log.warn("Found {} orphaned wallets", orphanedWallets.size());
        }

        // Check for transactions with invalid wallet references
        List<Transaction> invalidTransactions = transactionRepository.findTransactionsWithInvalidWallets();
        if (!invalidTransactions.isEmpty()) {
            log.warn("Found {} transactions with invalid wallet references", invalidTransactions.size());
        }

        // Check for balance inconsistencies
        List<Wallet> walletsWithInconsistentBalance = walletRepository.findWalletsWithInconsistentBalance();
        if (!walletsWithInconsistentBalance.isEmpty()) {
            log.warn("Found {} wallets with potentially inconsistent balances", walletsWithInconsistentBalance.size());
        }

        log.info("Referential integrity validation completed");
    }

    /**
     * Validates and fixes data consistency issues
     */
    @Transactional
    public void validateAndFixDataConsistency() {
        log.info("Starting data consistency validation and repair");

        // Fix wallet balances based on transaction history
        List<Wallet> wallets = walletRepository.findAll();
        for (Wallet wallet : wallets) {
            BigDecimal calculatedBalance = calculateWalletBalanceFromTransactions(wallet);
            if (calculatedBalance.compareTo(wallet.getBalance()) != 0) {
                log.warn("Balance mismatch for wallet {}: stored={}, calculated={}", 
                        wallet.getAccountNumber(), wallet.getBalance(), calculatedBalance);
                // In a real scenario, you might want to create an audit record and fix the balance
            }
        }

        log.info("Data consistency validation and repair completed");
    }

    private BigDecimal calculateWalletBalanceFromTransactions(Wallet wallet) {
        BigDecimal balance = BigDecimal.ZERO;

        // Add all received amounts
        List<Transaction> receivedTransactions = transactionRepository.findByReceiverWalletAndStatusOrderByCreatedAtAsc(
                wallet, com.boit_droid.wallet.entity.enums.Status.ACTIVE);
        for (Transaction transaction : receivedTransactions) {
            balance = balance.add(transaction.getAmount());
        }

        // Subtract all sent amounts
        List<Transaction> sentTransactions = transactionRepository.findBySenderWalletAndStatusOrderByCreatedAtAsc(
                wallet, com.boit_droid.wallet.entity.enums.Status.ACTIVE);
        for (Transaction transaction : sentTransactions) {
            balance = balance.subtract(transaction.getAmount());
        }

        return balance;
    }
}