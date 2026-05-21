package com.bankapp.bankingapp.application.interfaces.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface IRealtimeEventService {
    SseEmitter subscribeUser(Long userId);
    SseEmitter subscribeAdmin();
    void sendUserEvent(Long userId, String eventType, Object data);
    void sendAdminEvent(String eventType, Object data);
}
