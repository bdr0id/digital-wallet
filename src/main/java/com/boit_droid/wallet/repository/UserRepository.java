package com.boit_droid.wallet.repository;

import com.boit_droid.wallet.entity.User;
import com.boit_droid.wallet.entity.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface  UserRepository extends JpaRepository<User, Long> {

    // Find by unique identifiers
    Optional<User> findByRequestId(String requestId);
    Optional<User> findByMobile(String mobile);
    Optional<User> findByEmail(String email);
    Optional<User> findByIdNumber(String idNumber);

    // Check existence by unique fields
    boolean existsByMobile(String mobile);
    boolean existsByEmail(String email);
    boolean existsByIdNumber(String idNumber);
    boolean existsByRequestId(String requestId);

    // Find by status
    List<User> findByStatus(Status status);
    Page<User> findByStatus(Status status, Pageable pageable);

    // Find by KYC status
    List<User> findByKycStatus(String kycStatus);
    Page<User> findByKycStatus(String kycStatus, Pageable pageable);

    // Find users with pending KYC
    @Query("SELECT u FROM User u WHERE u.kycStatus = 'PENDING' ORDER BY u.createdAt ASC")
    List<User> findUsersWithPendingKYC();

    // Find verified users
    @Query("SELECT u FROM User u WHERE u.kycStatus = 'VERIFIED' AND u.status = :status")
    List<User> findVerifiedUsersByStatus(@Param("status") Status status);

    // Find users by creation date range
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate ORDER BY u.createdAt DESC")
    Page<User> findByCreatedAtBetween(@Param("startDate") Instant startDate,
                                     @Param("endDate") Instant endDate,
                                     Pageable pageable);

    // Find users by last login
    @Query("SELECT u FROM User u WHERE u.lastLoginAt IS NOT NULL AND u.lastLoginAt >= :since ORDER BY u.lastLoginAt DESC")
    List<User> findActiveUsersSince(@Param("since") Instant since);

    // Find users by country code
    List<User> findByCountryCode(String countryCode);

    // Search users by name
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(CONCAT(u.firstName, ' ', COALESCE(u.middleName, ''), ' ', COALESCE(u.lastName, ''))) " +
           "LIKE LOWER(CONCAT('%', :name, '%'))")
    List<User> findByNameContaining(@Param("name") String name);

    // Find users requiring KYC verification
    @Query("SELECT u FROM User u WHERE u.kycStatus IN ('PENDING', 'NOT_STARTED') AND u.status = 'ACTIVE'")
    List<User> findUsersRequiringKYCVerification();

    // Count users by status
    @Query("SELECT COUNT(u) FROM User u WHERE u.status = :status")
    Long countByStatus(@Param("status") Status status);

    // Count users by KYC status
    @Query("SELECT COUNT(u) FROM User u WHERE u.kycStatus = :kycStatus")
    Long countByKycStatus(@Param("kycStatus") String kycStatus);

    // Find users with soft delete
    @Query("SELECT u FROM User u WHERE u.isDeleted = true")
    List<User> findDeletedUsers();

    // Find users by creator for audit
    List<User> findByCreatedBy(String createdBy);
}
