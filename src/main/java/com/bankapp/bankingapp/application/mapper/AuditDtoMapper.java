package com.bankapp.bankingapp.application.mapper;

import com.bankapp.bankingapp.application.dto.response.AuditLogResponseDto;
import com.bankapp.bankingapp.domain.model.AuditLog;
import org.springframework.stereotype.Component;

/**
 * Mapper chuyển đổi giữa AuditLog (Domain Model) và AuditLogResponseDto (Data Transfer Object).
 * Nằm ở Application Layer, phục vụ cho việc ẩn giấu chi tiết của Domain Model khi trả dữ liệu ra Presentation Layer.
 */
@Component
public class AuditDtoMapper {

    /**
     * Chuyển đổi từ AuditLog (Domain Model) sang AuditLogResponseDto.
     *
     * @param log AuditLog (Domain Model)
     * @return AuditLogResponseDto
     */
    public AuditLogResponseDto toAuditLogResponseDto(AuditLog log) {
        if (log == null) {
            return null;
        }

        return AuditLogResponseDto.builder()
                .id(log.getId())
                .username(log.getPerformedBy())
                .action(log.getAction() != null ? log.getAction().name() : "UNKNOWN")
                .details(log.getDetails())
                .status(log.getStatus() != null ? log.getStatus().name() : "SUCCESS")
                .createdAt(log.getCreatedAt())
                .build();
    }
}
