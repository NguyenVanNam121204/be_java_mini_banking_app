package com.bankapp.bankingapp.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Bật async support để EmailServiceImpl có thể dùng @Async
 * Email sẽ được gửi trên thread riêng, không block request
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}
