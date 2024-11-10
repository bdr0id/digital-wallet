package com.boit_droid.wallet.entity;

import com.boit_droid.wallet.entity.audit.DateAudit;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_transaction_request_id", columnList = "requestId"),
        @Index(name = "idx_transaction_type", columnList = "type"),
        @Index(name = "idx_transaction_status", columnList = "status")
})
public class Transaction extends DateAudit implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String requestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender")
    private Wallet senderWallet; // Wallet sending the money, nullable for deposits

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receiver")
    private Wallet receiverWallet; // Wallet receiving the money

    @Column(nullable = false)
    private double amount;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private String type; // DEPOSIT, WITHDRAWAL, TRANSFER

    @Column(nullable = false)
    private String status; // PENDING, COMPLETED, FAILED, etc.

    private String description; // Optional description or note for the transaction
}
