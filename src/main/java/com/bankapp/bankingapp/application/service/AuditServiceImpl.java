package com.bankapp.bankingapp.application.service;

import com.bankapp.bankingapp.application.interfaces.repository.IAuditRepository;
import com.bankapp.bankingapp.application.interfaces.service.IAuditService;
import com.bankapp.bankingapp.domain.model.AuditLog;
import com.bankapp.bankingapp.domain.model.enums.AuditAction;
import com.bankapp.bankingapp.domain.model.enums.AuditStatus;
import org.springframework.stereotype.Service;

/**
 * AuditServiceImpl — application layer.
 * Chỉ phụ thuộc vào:
 *   - IAuditRepository (interface trong application layer)
 *   - AuditLog, AuditAction, AuditStatus (domain model)
 * KHÔNG import bất kỳ class nào từ infrastructure layer.
 */
@Service
public class AuditServiceImpl implements IAuditService {

    private final IAuditRepository auditRepository;

    public AuditServiceImpl(IAuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @Override
    public void logAction(String username, String action, String details) {
        // Parse action string thành enum — fallback về UNAUTHORIZED_ACCESS nếu không hợp lệ
        AuditAction auditAction;
        try {
            auditAction = AuditAction.valueOf(action.toUpperCase());
        } catch (IllegalArgumentException e) {
            auditAction = AuditAction.UNAUTHORIZED_ACCESS;
        }

        AuditLog auditLog = new AuditLog(
                null,
                auditAction,
                null,
                null,
                username,
                null,
                null,
                details,
                AuditStatus.SUCCESS
        );

        auditRepository.save(auditLog);
    }
}
