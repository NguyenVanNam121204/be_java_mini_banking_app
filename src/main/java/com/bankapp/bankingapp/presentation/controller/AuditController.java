package com.bankapp.bankingapp.presentation.controller;

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
        
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        
        PageResponseDto<AuditLogResponseDto> response = auditService.getAllLogsPaginated(pageRequest, username, action, date);
        return ResponseEntity.ok(ApiResponseDto.success("Lấy danh sách nhật ký thành công", response));
    }
}
