package com.bankapp.bankingapp.domain.model;

import com.bankapp.bankingapp.domain.model.enums.AuditAction;
import com.bankapp.bankingapp.domain.model.enums.AuditStatus;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Audit Log để tracking mọi hành động quan trọng trong hệ thống
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AuditLog {

    @Setter
    @EqualsAndHashCode.Include
    private Long id;

    private AuditAction action;
    private String entityType;      // "User", "Account", "Transaction"
    private Long entityId;

    private String performedBy;     // Username
    private String ipAddress;
    private String userAgent;

    private String details;         // JSON hoặc description
    private AuditStatus status;     // SUCCESS, FAILED

    private LocalDateTime createdAt;

    public AuditLog(Long id,
                    AuditAction action,
                    String entityType,
                    Long entityId,
                    String performedBy,
                    String ipAddress,
                    String userAgent,
                    String details,
                    AuditStatus status) {
        this.id = id;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.performedBy = performedBy;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.details = details;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    // BUSINESS LOGIC

    public boolean isSuccess() {
        return status == AuditStatus.SUCCESS;
    }

    public boolean isFailed() {
        return status == AuditStatus.FAILED;
    }

    public boolean isSecurityAction() {
        return action == AuditAction.LOGIN ||
               action == AuditAction.LOGOUT ||
               action == AuditAction.PASSWORD_CHANGE ||
               action == AuditAction.PIN_CHANGE ||
               action == AuditAction.ACCOUNT_LOCKED ||
               action == AuditAction.ACCOUNT_UNLOCKED;
    }

    public boolean isTransactionAction() {
        return action == AuditAction.DEPOSIT ||
               action == AuditAction.WITHDRAW ||
               action == AuditAction.TRANSFER;
    }
}
