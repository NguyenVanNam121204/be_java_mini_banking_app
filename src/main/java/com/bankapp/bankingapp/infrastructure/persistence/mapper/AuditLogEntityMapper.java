package com.bankapp.bankingapp.infrastructure.persistence.mapper;

import com.bankapp.bankingapp.domain.model.AuditLog;
import com.bankapp.bankingapp.domain.model.enums.AuditAction;
import com.bankapp.bankingapp.domain.model.enums.AuditStatus;
import com.bankapp.bankingapp.infrastructure.persistence.entity.AuditLogEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper chuyển đổi giữa AuditLog (domain) và AuditLogEntity (JPA).
 * Nằm trong infrastructure layer — đúng với Clean Architecture.
 */
@Component
public class AuditLogEntityMapper {

    /**
     * Chuyển AuditLog domain → AuditLogEntity để lưu vào DB.
     * AuditLogEntity hiện tại chỉ có username, action (String), details.
     * Ta map performedBy → username, action.name() → action.
     */
    public AuditLogEntity toEntity(AuditLog domain) {
        if (domain == null) return null;

        return AuditLogEntity.builder()
                .id(domain.getId())
                .username(domain.getPerformedBy())
                .action(domain.getAction() != null ? domain.getAction().name() : "UNKNOWN")
                .details(domain.getDetails())
                .build();
    }

    /**
     * Chuyển AuditLogEntity → AuditLog domain.
     */
    public AuditLog toDomain(AuditLogEntity entity) {
        if (entity == null) return null;

        AuditAction action;
        try {
            action = AuditAction.valueOf(entity.getAction());
        } catch (IllegalArgumentException e) {
            action = AuditAction.UNAUTHORIZED_ACCESS;
        }

        AuditLog log = new AuditLog(
                entity.getId(),
                action,
                null,
                null,
                entity.getUsername(),
                null,
                null,
                entity.getDetails(),
                AuditStatus.SUCCESS
        );
        return log;
    }
}
