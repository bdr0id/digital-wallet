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
@Table(name = "wallets",uniqueConstraints = {@UniqueConstraint(columnNames = {"accountNumber"}),@UniqueConstraint(columnNames = {"requestId"})})
public class Wallet extends DateAudit implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String requestId;

    private String accountName;

    @Column(nullable = false, unique = true)
    private String accountNumber;

    @Column(nullable = false)
    private double balance;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private String salt; // For cryptographic purposes

    @Column(nullable = false)
    private String signature; // For verifying transactions
}
