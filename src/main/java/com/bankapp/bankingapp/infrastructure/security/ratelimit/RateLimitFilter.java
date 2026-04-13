package com.bankapp.bankingapp.infrastructure.security.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final PathMatcher pathMatcher = new AntPathMatcher();
    private final List<String> rateLimitedPaths = Arrays.asList(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/forgot-password",
            "/api/auth/verify-email",
            "/api/auth/resend-verification",
            "/api/auth/reset-password"
    );

    public RateLimitFilter(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String requestPath = request.getRequestURI();
        String ipAddress = getClientIpAddress(request);

        // Check if this path requires rate limiting
        boolean isRateLimited = requestPath != null && rateLimitedPaths.stream()
                .anyMatch(path -> path != null && pathMatcher.match(path, requestPath));

        HttpServletRequest requestToUse = request;

        if (isRateLimited) {
            // Wrap request to cached body so it can be read multiple times (once here, once in controller)
            CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);
            requestToUse = cachedRequest;
            
            // Apply appropriate rate limit based on endpoint
            boolean allowed = checkRateLimit(requestPath, ipAddress, cachedRequest);

            if (!allowed) {
                response.setStatus(429); // Too Many Requests
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": false, \"message\": \"Too many requests. Please try again later.\"}");
                return;
            }
        }

        filterChain.doFilter(requestToUse, response);
    }

    private boolean checkRateLimit(String path, String ipAddress, HttpServletRequest request) {
        return switch (path) {
            case "/api/auth/login" -> rateLimitService.isLoginAllowed(ipAddress);
            case "/api/auth/verify-email" -> {
                String email = getEmailFromRequest(request);
                yield email != null ? rateLimitService.isOtpVerificationAllowed(email) : true;
            }
            case "/api/auth/forgot-password", "/api/auth/resend-verification" -> {
                String email = getEmailFromRequest(request);
                yield email != null ? rateLimitService.isOtpGenerationAllowed(email) : true;
            }
            case "/api/auth/reset-password" -> {
                String email = getEmailFromRequest(request);
                yield email != null ? rateLimitService.isOtpVerificationAllowed(email) : true;
            }
            default -> rateLimitService.isApiRequestAllowed(ipAddress);
        };
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String[] ipHeaders = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_MSERVE_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR"
        };

        for (String header : ipHeaders) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }

        String remoteAddr = request.getRemoteAddr();
        return remoteAddr != null ? remoteAddr : "unknown";
    }

    private String getEmailFromRequest(HttpServletRequest request) {
        try {
            String body = new String(request.getInputStream().readAllBytes());
            // Simple extraction of email from JSON body
            int emailIndex = body.indexOf("\"email\"");
            if (emailIndex != -1) {
                int colonIndex = body.indexOf(":", emailIndex);
                int quoteStart = body.indexOf("\"", colonIndex);
                int quoteEnd = body.indexOf("\"", quoteStart + 1);
                if (quoteEnd > quoteStart) {
                    return body.substring(quoteStart + 1, quoteEnd);
                }
            }
        } catch (Exception e) {
            // Silently fail
        }
        return null;
    }
}
