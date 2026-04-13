package com.bankapp.bankingapp.infrastructure.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class HttpRequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(HttpRequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        long start = System.currentTimeMillis();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = System.currentTimeMillis() - start;
            String method = request.getMethod();
            String uri = request.getRequestURI();
            String query = request.getQueryString();
            int status = response.getStatus();
            String user = resolveCurrentUser();

            if (query != null && !query.isBlank()) {
                uri = uri + "?" + query;
            }

            logger.info("HTTP {} {} -> {} ({} ms) user={}", method, uri, status, durationMs, user);
        }
    }

    private String resolveCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "anonymous";
        }

        String name = authentication.getName();
        if (name == null || name.isBlank() || "anonymousUser".equals(name)) {
            return "anonymous";
        }

        return name;
    }
}
