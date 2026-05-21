package com.bankapp.bankingapp.presentation.controller;

import com.bankapp.bankingapp.application.dto.response.ApiResponseDto;
import com.bankapp.bankingapp.application.dto.response.NotificationResponseDto;
import com.bankapp.bankingapp.application.dto.response.PageResponseDto;
import com.bankapp.bankingapp.application.interfaces.repository.IUserRepository;
import com.bankapp.bankingapp.application.interfaces.service.INotificationService;
import com.bankapp.bankingapp.application.interfaces.service.IRealtimeEventService;
import org.springframework.http.MediaType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "APIs thong bao cho nguoi dung")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final INotificationService notificationService;
    private final IRealtimeEventService realtimeEventService;
    private final IUserRepository userRepository;

    public NotificationController(INotificationService notificationService,
                                  IRealtimeEventService realtimeEventService,
                                  IUserRepository userRepository) {
        this.notificationService = notificationService;
        this.realtimeEventService = realtimeEventService;
        this.userRepository = userRepository;
    }

    @Operation(summary = "Mo kenh SSE thong bao realtime cho user hien tai")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamNotifications(Authentication authentication) {
        String principal = authentication.getName();
        Long userId = userRepository.findByUsername(principal)
                .or(() -> userRepository.findByEmail(principal))
                .orElseThrow(() -> new IllegalArgumentException("User not found"))
                .getId();
        return realtimeEventService.subscribeUser(userId);
    }

    @Operation(summary = "Lay danh sach thong bao cua user hien tai")
    @GetMapping
    public ResponseEntity<ApiResponseDto<PageResponseDto<NotificationResponseDto>>> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        PageResponseDto<NotificationResponseDto> response = notificationService.getCurrentUserNotifications(page, size);
        return ResponseEntity.ok(ApiResponseDto.success("Lay danh sach thong bao thanh cong", response));
    }

    @Operation(summary = "Danh dau 1 thong bao la da doc")
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponseDto<Void>> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok(ApiResponseDto.success("Danh dau da doc thanh cong", null));
    }

    @Operation(summary = "Danh dau tat ca thong bao la da doc")
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponseDto<Void>> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok(ApiResponseDto.success("Danh dau tat ca la da doc thanh cong", null));
    }

    @Operation(summary = "Xoa 1 thong bao")
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponseDto<Void>> deleteNotification(@PathVariable Long notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.ok(ApiResponseDto.success("Xoa thong bao thanh cong", null));
    }
}
