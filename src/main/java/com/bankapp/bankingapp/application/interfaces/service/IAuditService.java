package com.bankapp.bankingapp.application.interfaces.service;

import com.bankapp.bankingapp.application.dto.response.AuditLogResponseDto;
import com.bankapp.bankingapp.application.dto.response.PageResponseDto;
import org.springframework.data.domain.Pageable;

public interface IAuditService {
    void logAction(String username, String action, String details);
    PageResponseDto<AuditLogResponseDto> getAllLogsPaginated(Pageable pageable, String username, String action, String date);
}
