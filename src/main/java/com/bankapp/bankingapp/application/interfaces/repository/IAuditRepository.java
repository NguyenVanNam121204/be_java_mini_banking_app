package com.bankapp.bankingapp.application.interfaces.repository;

import com.bankapp.bankingapp.domain.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Repository interface cho AuditLog — nằm ở application layer.
 * Infrastructure sẽ implement interface này, application service chỉ phụ thuộc vào interface.
 */
public interface IAuditRepository {

    AuditLog save(AuditLog auditLog);
    Page<AuditLog> findAll(Pageable pageable, String username, String action, String date);
}
