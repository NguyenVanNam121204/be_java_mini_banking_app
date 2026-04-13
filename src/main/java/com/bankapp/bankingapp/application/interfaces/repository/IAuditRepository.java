package com.bankapp.bankingapp.application.interfaces.repository;

import com.bankapp.bankingapp.domain.model.AuditLog;

/**
 * Repository interface cho AuditLog — nằm ở application layer.
 * Infrastructure sẽ implement interface này, application service chỉ phụ thuộc vào interface.
 */
public interface IAuditRepository {

    AuditLog save(AuditLog auditLog);
}
