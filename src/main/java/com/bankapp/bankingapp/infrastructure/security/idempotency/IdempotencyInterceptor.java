package com.bankapp.bankingapp.infrastructure.security.idempotency;

import com.bankapp.bankingapp.infrastructure.persistence.entity.IdempotencyKeyEntity;
import com.bankapp.bankingapp.infrastructure.persistence.jpaRepository.IdempotencyKeyJpaRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class IdempotencyInterceptor implements HandlerInterceptor {

    private final IdempotencyKeyJpaRepository idempotencyKeyJpaRepository;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        Idempotent idempotent = handlerMethod.getMethodAnnotation(Idempotent.class);
        if (idempotent == null) {
            return true;
        }

        String idempotencyKey = request.getHeader("Idempotency-Key");

        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Bad Request",
                    "Header 'Idempotency-Key' is required for this transaction.", request.getRequestURI());
            return false;
        }

        if (idempotencyKey.length() > 100) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Bad Request",
                    "Header 'Idempotency-Key' is too long. Maximum length is 100 characters.", request.getRequestURI());
            return false;
        }

        if (idempotencyKeyJpaRepository.existsById(idempotencyKey)) {
            writeError(response, HttpServletResponse.SC_CONFLICT, "Conflict",
                    "A transaction with this Idempotency-Key already exists or is being processed.", request.getRequestURI());
            return false;
        }

        try {
            idempotencyKeyJpaRepository.saveAndFlush(new IdempotencyKeyEntity(idempotencyKey, LocalDateTime.now()));
        } catch (DataIntegrityViolationException ex) {
            writeError(response, HttpServletResponse.SC_CONFLICT, "Conflict",
                    "A transaction with this Idempotency-Key is being processed.", request.getRequestURI());
            return false;
        }

        return true;
    }

    private void writeError(HttpServletResponse response, int status, String error, String message, String path) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(String.format(
                "{\"status\":%d,\"success\":false,\"error\":\"%s\",\"message\":\"%s\",\"path\":\"%s\"}",
                status, error, message, path));
    }
}
