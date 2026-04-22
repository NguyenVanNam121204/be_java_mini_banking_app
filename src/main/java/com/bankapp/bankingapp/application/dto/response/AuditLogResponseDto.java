package com.bankapp.bankingapp.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponseDto {
    private Long id;
    private String username;
    private String action;
    private String details;
    private String status;
    private LocalDateTime createdAt;
}
