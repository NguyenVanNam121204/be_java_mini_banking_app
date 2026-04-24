package com.bankapp.bankingapp.application.interfaces.service;

import com.bankapp.bankingapp.application.dto.request.AuditLogFilterRequestDto;
import com.bankapp.bankingapp.application.dto.response.AuditLogResponseDto;
import com.bankapp.bankingapp.application.dto.response.PageResponseDto;
import com.bankapp.bankingapp.domain.model.enums.AuditAction;
import org.springframework.data.domain.Pageable;

public interface IAuditService {
    /**
     * Ghi Audit Log — nhan AuditAction enum de dam bao type-safe tai compile time.
     */
    void logAction(String username, AuditAction action, String details);
    PageResponseDto<AuditLogResponseDto> getAllLogsPaginated(Pageable pageable, AuditLogFilterRequestDto filter);

    /**
     * Export all audit logs matching the filter to CSV format as a byte array.
     */
    byte[] exportLogsToCsv(AuditLogFilterRequestDto filter, org.springframework.data.domain.Sort sort);
}
