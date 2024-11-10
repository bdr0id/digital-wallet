package com.boit_droid.wallet.entity;

import com.boit_droid.wallet.entity.audit.DateAudit;
import com.boit_droid.wallet.entity.enums.Gender;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@Entity
@Table(name = "users",uniqueConstraints = {@UniqueConstraint(columnNames = {"idNumber"}),@UniqueConstraint(columnNames = {"requestId"}),@UniqueConstraint(columnNames = {"mobile"})})
public class User extends DateAudit implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String requestId;

    @Column(nullable = false)
    private String firstName;

    private String middleName;
    private String lastName;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(length = 5)
    private String countryCode;

    @Column(nullable = false, unique = true)
    private String mobile;

    @Column(nullable = false, unique = true)
    private String idNumber;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(length = 5)
    private String locale;

    @Column(nullable = false)
    private String status; // ACTIVE, INACTIVE, SUSPENDED, etc.
}
