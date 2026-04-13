package com.bankapp.bankingapp.infrastructure.config;

import com.bankapp.bankingapp.infrastructure.security.idempotency.IdempotencyInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    @NonNull
    private final IdempotencyInterceptor idempotencyInterceptor;

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(idempotencyInterceptor)
                .addPathPatterns("/api/**");
    }
}
