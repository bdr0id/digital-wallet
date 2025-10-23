package com.boit_droid.wallet.entity.audit;

import com.boit_droid.wallet.entity.User;
import com.boit_droid.wallet.entity.Wallet;
import com.boit_droid.wallet.entity.Transaction;
import com.boit_droid.wallet.entity.Notification;
import com.boit_droid.wallet.repository.AuditTrailRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
public class AuditTrailEntityListener {

    private static ObjectMapper objectMapper;
    private static AuditTrailRepository auditTrailRepository;

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        AuditTrailEntityListener.objectMapper = objectMapper;
    }

    @Autowired
    public void setAuditTrailRepository(AuditTrailRepository auditTrailRepository) {
        AuditTrailEntityListener.auditTrailRepository = auditTrailRepository;
    }

    @PrePersist
    public void prePersist(Object entity) {
        try {
            if (entity instanceof DateAudit dateAuditEntity) {
                String auditId = UUID.randomUUID().toString();
                dateAuditEntity.setAuditTrailId(auditId);
                dateAuditEntity.setOperationType("CREATE");
                
                createAuditTrail(entity, "CREATE", null, entity, auditId);
            }
        } catch (Exception e) {
            log.error("Error in prePersist audit trail", e);
        }
    }

    @PreUpdate
    public void preUpdate(Object entity) {
        try {
            if (entity instanceof DateAudit dateAuditEntity) {
                String auditId = UUID.randomUUID().toString();
                dateAuditEntity.setAuditTrailId(auditId);
                dateAuditEntity.setOperationType("UPDATE");
                
                // Note: In a real implementation, you would need to fetch the old values
                // This is a simplified version
                createAuditTrail(entity, "UPDATE", null, entity, auditId);
            }
        } catch (Exception e) {
            log.error("Error in preUpdate audit trail", e);
        }
    }

    @PreRemove
    public void preRemove(Object entity) {
        try {
            if (entity instanceof DateAudit dateAuditEntity) {
                String auditId = UUID.randomUUID().toString();
                dateAuditEntity.setAuditTrailId(auditId);
                dateAuditEntity.setOperationType("DELETE");
                
                createAuditTrail(entity, "DELETE", entity, null, auditId);
            }
        } catch (Exception e) {
            log.error("Error in preRemove audit trail", e);
        }
    }

    private void createAuditTrail(Object entity, String operation, Object oldValues, Object newValues, String auditId) {
        try {
            if (auditTrailRepository == null) {
                log.warn("AuditTrailRepository not available, skipping audit trail creation");
                return;
            }

            AuditTrail auditTrail = new AuditTrail();
            auditTrail.setAuditId(auditId);
            auditTrail.setOperation(operation);
            auditTrail.setTimestamp(Instant.now());
            auditTrail.setCreatedAt(Instant.now());
            auditTrail.setIsSuccessful(true);

            // Set entity type and ID based on entity type
            if (entity instanceof User user) {
                auditTrail.setEntityType("User");
                auditTrail.setEntityId(user.getRequestId());
                auditTrail.setUserId(user.getRequestId());
                auditTrail.setUsername(user.getFirstName() + " " + user.getLastName());
            } else if (entity instanceof Wallet wallet) {
                auditTrail.setEntityType("Wallet");
                auditTrail.setEntityId(wallet.getRequestId());
                auditTrail.setUserId(wallet.getUser() != null ? wallet.getUser().getRequestId() : null);
            } else if (entity instanceof Transaction transaction) {
                auditTrail.setEntityType("Transaction");
                auditTrail.setEntityId(transaction.getRequestId());
                auditTrail.setTransactionId(transaction.getRequestId());
                auditTrail.setTransactionType(transaction.getType() != null ? transaction.getType().name() : null);
                auditTrail.setTransactionAmount(transaction.getAmount());
                auditTrail.setCurrency(transaction.getCurrency());
                auditTrail.setContainsFinancialData(true);
                auditTrail.setIsSensitiveOperation(true);
            } else if (entity instanceof Notification notification) {
                auditTrail.setEntityType("Notification");
                auditTrail.setEntityId(notification.getRequestId());
                auditTrail.setUserId(notification.getUser() != null ? notification.getUser().getRequestId() : null);
            }

            // Serialize old and new values
            if (oldValues != null && objectMapper != null) {
                auditTrail.setOldValues(objectMapper.writeValueAsString(oldValues));
            }
            if (newValues != null && objectMapper != null) {
                auditTrail.setNewValues(objectMapper.writeValueAsString(newValues));
            }

            // Set default values
            auditTrail.setSourceSystem("SYSTEM");
            auditTrail.setDataClassification("INTERNAL");
            auditTrail.setRiskLevel("LOW");
            
            // Save audit trail asynchronously to avoid impacting main transaction
            auditTrailRepository.save(auditTrail);
            
        } catch (Exception e) {
            log.error("Failed to create audit trail for {} operation on {}", operation, entity.getClass().getSimpleName(), e);
        }
    }
}