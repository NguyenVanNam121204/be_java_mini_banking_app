package com.bankapp.bankingapp.infrastructure.realtime;

import com.bankapp.bankingapp.application.dto.response.RealtimeEventDto;
import com.bankapp.bankingapp.application.interfaces.service.IRealtimeEventService;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class RealtimeEventServiceImpl implements IRealtimeEventService {

    private static final long SSE_TIMEOUT_MS = 0L;

    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> userEmitters = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<SseEmitter> adminEmitters = new CopyOnWriteArrayList<>();

    @Override
    public SseEmitter subscribeUser(Long userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        userEmitters.computeIfAbsent(userId, ignored -> new CopyOnWriteArrayList<>()).add(emitter);
        registerLifecycle(emitter, userEmitters.get(userId));
        sendInitialEvent(emitter, "CONNECTED", Map.of("scope", "USER"));
        return emitter;
    }

    @Override
    public SseEmitter subscribeAdmin() {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        adminEmitters.add(emitter);
        registerLifecycle(emitter, adminEmitters);
        sendInitialEvent(emitter, "CONNECTED", Map.of("scope", "ADMIN"));
        return emitter;
    }

    @Override
    public void sendUserEvent(Long userId, String eventType, Object data) {
        List<SseEmitter> emitters = userEmitters.get(userId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }
        emitToTargets(emitters, eventType, data);
    }

    @Override
    public void sendAdminEvent(String eventType, Object data) {
        emitToTargets(adminEmitters, eventType, data);
    }

    private void emitToTargets(List<SseEmitter> emitters, String eventType, Object data) {
        RealtimeEventDto payload = RealtimeEventDto.builder()
                .eventType(eventType)
                .data(data)
                .occurredAt(LocalDateTime.now())
                .build();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(eventType).data(payload));
            } catch (IOException ex) {
                emitter.complete();
                emitters.remove(emitter);
            }
        }
    }

    private void sendInitialEvent(SseEmitter emitter, String eventType, Object data) {
        try {
            emitter.send(SseEmitter.event().name(eventType).data(
                    RealtimeEventDto.builder()
                            .eventType(eventType)
                            .data(data)
                            .occurredAt(LocalDateTime.now())
                            .build()));
        } catch (IOException ex) {
            emitter.complete();
        }
    }

    private void registerLifecycle(SseEmitter emitter, List<SseEmitter> bucket) {
        emitter.onCompletion(() -> bucket.remove(emitter));
        emitter.onTimeout(() -> {
            emitter.complete();
            bucket.remove(emitter);
        });
        emitter.onError(ex -> {
            emitter.complete();
            bucket.remove(emitter);
        });
    }
}
