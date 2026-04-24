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

import org.springframework.data.domain.Sort;

import com.bankapp.bankingapp.application.mapper.AuditDtoMapper;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
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
    private final AuditDtoMapper auditDtoMapper;

    public AuditServiceImpl(IAuditRepository auditRepository, AuditDtoMapper auditDtoMapper) {
        this.auditRepository = auditRepository;
        this.auditDtoMapper = auditDtoMapper;
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
                .map(auditDtoMapper::toAuditLogResponseDto)
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

    @Override
    public byte[] exportLogsToCsv(AuditLogFilterRequestDto filter, Sort sort) {
        List<AuditLog> logs = auditRepository.findAllForExport(
                filter != null ? filter.getUsername() : null,
                filter != null ? filter.getActionGroup() : null,
                filter != null ? filter.getDate() : null,
                sort
        );

        StringBuilder csv = new StringBuilder();
        // UTF-8 BOM for Excel compatibility
        csv.append('\ufeff');
        
        // Header
        csv.append("ID,Thời Gian,Người Thực Hiện,Hành Động,Nội Dung Chi Tiết,Trạng Thái\n");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (AuditLog log : logs) {
            csv.append(log.getId()).append(",");
            csv.append(log.getCreatedAt() != null ? log.getCreatedAt().format(formatter) : "").append(",");
            csv.append(escapeCsv(log.getPerformedBy())).append(",");
            csv.append(log.getAction() != null ? log.getAction().name() : "UNKNOWN").append(",");
            csv.append(escapeCsv(log.getDetails())).append(",");
            csv.append(log.getStatus() != null ? log.getStatus().name() : "SUCCESS").append("\n");
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
