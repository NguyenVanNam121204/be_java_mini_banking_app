package com.bankapp.bankingapp.presentation.controller;

import com.bankapp.bankingapp.application.dto.response.ApiResponseDto;
import com.bankapp.bankingapp.application.dto.response.DashboardStatsResponseDto;
import com.bankapp.bankingapp.application.interfaces.service.IDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@Tag(name = "Admin Dashboard", description = "Các API hiển thị thông số thống kê cho Dashboard Admin")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class DashboardController {

    private final IDashboardService dashboardService;

    @Operation(summary = "Lấy thông số thống kê Dashboard", description = "Trả về tổng số user, giao dịch trong ngày và dữ liệu biểu đồ")
    @GetMapping("/stats")
    public ResponseEntity<ApiResponseDto<DashboardStatsResponseDto>> getStats() {
        DashboardStatsResponseDto stats = dashboardService.getDashboardStats();
        return ResponseEntity.ok(ApiResponseDto.success("Lấy thông số thống kê thành công", stats));
    }
}
