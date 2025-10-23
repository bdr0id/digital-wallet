package com.boit_droid.wallet.repository;

import com.boit_droid.wallet.entity.audit.AuditTrail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuditTrailRepository extends JpaRepository<AuditTrail, Long> {

    // Find audit records by entity
    List<AuditTrail> findByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, String entityId);

    // Find audit records by user
    Page<AuditTrail> findByUserIdOrderByTimestampDesc(String userId, Pageable pageable);

    // Find audit records by operation type
    List<AuditTrail> findByOperationOrderByTimestampDesc(String operation);

    // Find audit records by transaction ID
    List<AuditTrail> findByTransactionIdOrderByTimestampDesc(String transactionId);

    // Find audit records within date range
    @Query("SELECT a FROM AuditTrail a WHERE a.timestamp BETWEEN :startDate AND :endDate ORDER BY a.timestamp DESC")
    Page<AuditTrail> findByTimestampBetween(@Param("startDate") Instant startDate, 
                                           @Param("endDate") Instant endDate, 
                                           Pageable pageable);

    // Find sensitive operations
    List<AuditTrail> findByIsSensitiveOperationTrueOrderByTimestampDesc();

    // Find failed operations
    List<AuditTrail> findByIsSuccessfulFalseOrderByTimestampDesc();

    // Find operations requiring approval
    //List<AuditTrail> findByRequiresApprovalTrueAndIsApprovedFalseOrderByTimestampDesc();

    // Find audit records by session
    List<AuditTrail> findBySessionIdOrderByTimestampDesc(String sessionId);

    // Find audit records by source system
    Page<AuditTrail> findBySourceSystemOrderByTimestampDesc(String sourceSystem, Pageable pageable);

    // Find audit records by risk level
    List<AuditTrail> findByRiskLevelOrderByTimestampDesc(String riskLevel);

    // Find audit records with alerts
    List<AuditTrail> findByAlertGeneratedTrueOrderByTimestampDesc();

    // Custom query for comprehensive audit search
    @Query("SELECT a FROM AuditTrail a WHERE " +
           "(:entityType IS NULL OR a.entityType = :entityType) AND " +
           "(:userId IS NULL OR a.userId = :userId) AND " +
           "(:operation IS NULL OR a.operation = :operation) AND " +
           "(:sourceSystem IS NULL OR a.sourceSystem = :sourceSystem) AND " +
           "(:startDate IS NULL OR a.timestamp >= :startDate) AND " +
           "(:endDate IS NULL OR a.timestamp <= :endDate) " +
           "ORDER BY a.timestamp DESC")
    Page<AuditTrail> findAuditRecords(@Param("entityType") String entityType,
                                     @Param("userId") String userId,
                                     @Param("operation") String operation,
                                     @Param("sourceSystem") String sourceSystem,
                                     @Param("startDate") Instant startDate,
                                     @Param("endDate") Instant endDate,
                                     Pageable pageable);

    // Find audit trail by correlation ID for distributed tracing
    List<AuditTrail> findByCorrelationIdOrderByTimestampDesc(String correlationId);

    // Find audit records by batch ID
    List<AuditTrail> findByBatchIdOrderByTimestampDesc(String batchId);

    // Count operations by user within time period
    @Query("SELECT COUNT(a) FROM AuditTrail a WHERE a.userId = :userId AND a.timestamp BETWEEN :startDate AND :endDate")
    Long countOperationsByUserInPeriod(@Param("userId") String userId, 
                                      @Param("startDate") Instant startDate, 
                                      @Param("endDate") Instant endDate);

    // Find the latest audit record for an entity
    Optional<AuditTrail> findFirstByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, String entityId);

    // Find audit records containing PII for compliance
    Page<AuditTrail> findByContainsPIITrueOrderByTimestampDesc(Pageable pageable);

    // Find audit records by jurisdiction for regulatory compliance
    List<AuditTrail> findByJurisdictionOrderByTimestampDesc(String jurisdiction);

    // Find expired audit records for cleanup
    @Query("SELECT a FROM AuditTrail a WHERE a.retentionExpiryDate IS NOT NULL AND a.retentionExpiryDate <= :currentDate")
    List<AuditTrail> findExpiredAuditRecords(@Param("currentDate") Instant currentDate);
}