package com.boit_droid.wallet.entity;

import com.boit_droid.wallet.entity.audit.DateAudit;
import com.boit_droid.wallet.entity.enums.Gender;
import com.boit_droid.wallet.entity.enums.Status;
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Index;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.*;
import org.hibernate.annotations.Cache;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true, exclude = {"wallets", "notifications"})
@ToString(callSuper = true, exclude = {"wallets", "notifications"})
@Getter
@Setter
@NoArgsConstructor
//@SQLDelete(sql = "UPDATE users SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
//@FilterDef(name = "deletedFilter", parameters = @ParamDef(name = "isDeleted", type = Boolean.class))
//@Filter(name = "deletedFilter", condition = "is_deleted = :isDeleted")
@Entity
@Table(name = "users", 
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_user_id_number", columnNames = {"idNumber"}),
           @UniqueConstraint(name = "uk_user_request_id", columnNames = {"requestId"}),
           @UniqueConstraint(name = "uk_user_mobile", columnNames = {"mobile"}),
           @UniqueConstraint(name = "uk_user_email", columnNames = {"email"})
       },
       indexes = {
           @Index(name = "idx_user_status", columnList = "status"),
           @Index(name = "idx_user_kyc_status", columnList = "kycStatus"),
           @Index(name = "idx_user_mobile", columnList = "mobile"),
           @Index(name = "idx_user_email", columnList = "email"),
           @Index(name = "idx_user_request_id", columnList = "requestId"),
           @Index(name = "idx_user_full_name", columnList = "firstName, lastName"),
           @Index(name = "idx_user_country_mobile", columnList = "countryCode, mobile"),
           @Index(name = "idx_user_kyc_verified_at", columnList = "kycVerifiedAt"),
           @Index(name = "idx_user_last_login", columnList = "lastLoginAt"),
           @Index(name = "idx_user_composite_status", columnList = "status, kycStatus, isDeleted")
       })
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class User extends DateAudit implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Request ID is required")
    @Size(max = 36, message = "Request ID must not exceed 36 characters")
    @Column(nullable = false, unique = true, length = 36)
    private String requestId;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "First name can only contain letters, spaces, hyphens, and apostrophes")
    @Column(nullable = false, length = 100)
    private String firstName;

    @Size(max = 100, message = "Middle name must not exceed 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]*$", message = "Middle name can only contain letters, spaces, hyphens, and apostrophes")
    @Column(length = 100)
    private String middleName;
    
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]*$", message = "Last name can only contain letters, spaces, hyphens, and apostrophes")
    @Column(length = 100)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    @Size(max = 5, message = "Country code must not exceed 5 characters")
    @Pattern(regexp = "^\\+?[0-9]{1,4}$", message = "Invalid country code format")
    @Column(length = 5)
    private String countryCode;

    @NotBlank(message = "Mobile number is required")
    @Size(min = 9, max = 15, message = "Mobile number must be between 9 and 15 characters")
    @Pattern(regexp = "^[0-9]+$", message = "Mobile number can only contain digits")
    @Column(nullable = false, unique = true, length = 15)
    private String mobile;

    @NotBlank(message = "ID number is required")
    @Size(min = 5, max = 50, message = "ID number must be between 5 and 50 characters")
    @Column(nullable = false, unique = true, length = 50)
    private String idNumber;

    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Column(unique = true, length = 255)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
    @Column(nullable = false, length = 255)
    private String password;

    @Size(max = 5, message = "Locale must not exceed 5 characters")
    @Pattern(regexp = "^[a-z]{2}(_[A-Z]{2})?$", message = "Invalid locale format (e.g., en, en_US)")
    @Column(length = 5)
    private String locale;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status; // ACTIVE, PENDING, LOCKED, CLOSED, etc.

    // KYC related fields
    @Column(nullable = false, length = 20)
    private String kycStatus; // PENDING, VERIFIED, REJECTED, NOT_STARTED

//    @Column(name = "is_deleted", nullable = false)
//    private boolean isDeleted = false;
//
//    @Column(name = "deleted_at")
//    private Instant deletedAt;

    @Column(length = 100)
    private String kycDocumentType;

    @Column(length = 100)
    private String kycDocumentNumber;

    @Column(length = 500)
    private String kycRejectionReason;

    private Instant kycVerifiedAt;

    @Column(length = 100)
    private String kycVerifiedBy;

    // Audit trail fields
    @Column(length = 50)
    private String lastLoginIp;

    private Instant lastLoginAt;

    @Column(length = 100)
    private String createdBy;

    @Column(length = 100)
    private String lastModifiedBy;

    // Relationships with enhanced configuration
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @OrderBy("createdAt DESC")
    private List<Wallet> wallets = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @OrderBy("createdAt DESC")
    private List<Notification> notifications = new ArrayList<>();

    // Helper methods for relationship management
    public void addWallet(Wallet wallet) {
        wallets.add(wallet);
        wallet.setUser(this);
    }

    public void removeWallet(Wallet wallet) {
        wallets.remove(wallet);
        wallet.setUser(null);
    }

    public void addNotification(Notification notification) {
        notifications.add(notification);
        notification.setUser(this);
    }

    public void removeNotification(Notification notification) {
        notifications.remove(notification);
        notification.setUser(null);
    }

    public String getUserId() {
        return String.valueOf(id);
    }
}
