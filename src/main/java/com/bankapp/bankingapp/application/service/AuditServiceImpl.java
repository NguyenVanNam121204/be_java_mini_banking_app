package com.bankapp.bankingapp.application.service;

import com.bankapp.bankingapp.application.interfaces.repository.IAuditRepository;
import com.bankapp.bankingapp.application.interfaces.service.IAuditService;
import com.bankapp.bankingapp.application.dto.request.AuditLogFilterRequestDto;
import com.bankapp.bankingapp.domain.model.AuditLog;
import com.bankapp.bankingapp.domain.model.enums.AuditAction;
import com.bankapp.bankingapp.domain.model.enums.AuditStatus;
import com.bankapp.bankingapp.application.dto.response.AuditLogResponseDto;
import com.bankapp.bankingapp.application.dto.response.PageResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

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
    public PageResponseDto<AuditLogResponseDto> getAllLogsPaginated(Pageable pageable, AuditLogFilterRequestDto filter) {
        // Unpack DTO - Service layer lay du lieu tu DTO roi truyen xuong Repository
        Page<AuditLog> logPage = auditRepository.findAll(
                pageable,
                filter != null ? filter.getUsername() : null,
                filter != null ? filter.getActionGroup() : null,
                filter != null ? filter.getDate() : null
        );
        
        List<AuditLogResponseDto> content = logPage.getContent().stream()
                .map(log -> AuditLogResponseDto.builder()
                        .id(log.getId())
                        .username(log.getPerformedBy())
                        .action(log.getAction() != null ? log.getAction().name() : "UNKNOWN")
                        .details(log.getDetails())
                        .status(log.getStatus() != null ? log.getStatus().name() : "SUCCESS")
                        .createdAt(log.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return PageResponseDto.<AuditLogResponseDto>builder()
                .content(content)
                .pageNo(logPage.getNumber())
                .pageSize(logPage.getSize())
                .totalElements(logPage.getTotalElements())
                .totalPages(logPage.getTotalPages())
                .last(logPage.isLast())
                .build();
    }

    @Override
    public void logAction(String username, AuditAction action, String details) {
        AuditLog auditLog = new AuditLog(
                null,
                action,
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
