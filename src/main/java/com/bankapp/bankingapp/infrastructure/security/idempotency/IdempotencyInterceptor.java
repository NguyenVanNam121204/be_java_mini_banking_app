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
        if (handler instanceof HandlerMethod handlerMethod) {
            Idempotent idempotent = handlerMethod.getMethodAnnotation(Idempotent.class);
            if (idempotent != null) {
                String idempotencyKey = request.getHeader("Idempotency-Key");
                
                if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"status\":400,\"error\":\"Bad Request\",\"message\":\"Header 'Idempotency-Key' là bắt buộc cho giao dịch này.\",\"path\":\"" + request.getRequestURI() + "\"}");
                    return false;
                }

                if (idempotencyKey.length() > 100) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"status\":400,\"error\":\"Bad Request\",\"message\":\"Header 'Idempotency-Key' quá dài (tối đa 100 ký tự).\",\"path\":\"" + request.getRequestURI() + "\"}");
                    return false;
                }

                // Kiểm tra xem key đã tồn tại chưa (Giao dịch đang xử lý hoặc đã xong)
                if (idempotencyKeyJpaRepository.existsById(idempotencyKey)) {
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"status\":409,\"error\":\"Conflict\",\"message\":\"Giao dịch có Idempotency-Key này đã tồn tại hoặc đang được xử lý.\",\"path\":\"" + request.getRequestURI() + "\"}");
                    return false;
                }

                try {
                    // Cố gắng insert key vào DB, nếu có 2 luồng cùng lúc insert sẽ văng DataIntegrityViolationException
                    idempotencyKeyJpaRepository.saveAndFlush(new IdempotencyKeyEntity(idempotencyKey, LocalDateTime.now()));
                } catch (DataIntegrityViolationException ex) {
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"status\":409,\"error\":\"Conflict\",\"message\":\"Giao dịch có Idempotency-Key này đang được xử lý.\",\"path\":\"" + request.getRequestURI() + "\"}");
                    return false;
                }
            }
        }
        return true;
    }
}
