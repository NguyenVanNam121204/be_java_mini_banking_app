package com.bankapp.bankingapp.presentation.controller;

import com.bankapp.bankingapp.application.interfaces.service.IRealtimeEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/admin/events")
@Tag(name = "Admin Realtime", description = "SSE realtime cho admin")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRealtimeController {

    private final IRealtimeEventService realtimeEventService;

    public AdminRealtimeController(IRealtimeEventService realtimeEventService) {
        this.realtimeEventService = realtimeEventService;
    }

    @Operation(summary = "Mo kenh SSE realtime cho admin")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamAdminEvents() {
        return realtimeEventService.subscribeAdmin();
    }
}
