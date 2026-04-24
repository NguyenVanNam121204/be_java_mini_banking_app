package com.bankapp.bankingapp.presentation.controller;

import com.bankapp.bankingapp.application.dto.request.AuditLogFilterRequestDto;
import com.bankapp.bankingapp.application.dto.response.ApiResponseDto;
import com.bankapp.bankingapp.application.dto.response.AuditLogResponseDto;
import com.bankapp.bankingapp.application.dto.response.PageResponseDto;
import com.bankapp.bankingapp.application.interfaces.service.IAuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Admin Audit Logs", description = "Các API tra cứu nhật ký hệ thống dành cho ADMIN")
@SecurityRequirement(name = "bearerAuth")
public class AuditController {

    private final IAuditService auditService;

    @Operation(summary = "Xem nhật ký hệ thống", description = "Lấy danh sách các hoạt động của người dùng và hệ thống với bộ lọc")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDto<PageResponseDto<AuditLogResponseDto>>> getAllLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String date) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        // Controller chiu trach nhiem dong goi request params thanh DTO
        AuditLogFilterRequestDto filter = AuditLogFilterRequestDto.builder()
                .username(username)
                .actionGroup(action)
                .date(date)
                .build();

        PageResponseDto<AuditLogResponseDto> response = auditService.getAllLogsPaginated(pageRequest, filter);
        return ResponseEntity.ok(ApiResponseDto.success("Lấy danh sách nhật ký thành công", response));
    }

    @Operation(summary = "Xuất báo cáo nhật ký", description = "Xuất file CSV chứa tất cả nhật ký hệ thống thỏa mãn điều kiện lọc")
    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportLogs(
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String date) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        AuditLogFilterRequestDto filter = AuditLogFilterRequestDto.builder()
                .username(username)
                .actionGroup(action)
                .date(date)
                .build();

        byte[] csvData = auditService.exportLogsToCsv(filter, sort);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"audit_report.csv\"");
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=utf-8"));

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvData);
    }
}
