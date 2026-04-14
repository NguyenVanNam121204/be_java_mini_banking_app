package com.bankapp.bankingapp.infrastructure.security.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for rate limiting using Bucket4j (Token Bucket Algorithm)
 */
@Service
public class RateLimitService {

    // Store buckets per key (IP address, email, etc.)
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Check if request is allowed for given key with specified rate limit
     *
     * @param key     Unique identifier (IP, email, userId, etc.)
     * @param capacity Maximum tokens in bucket
     * @param refillTokens How many tokens to refill
     * @param refillDuration Duration for refill
     * @return true if allowed, false if rate limit exceeded
     */
    public boolean tryConsume(String key, long capacity, long refillTokens, Duration refillDuration) {
        Bucket bucket = buckets.computeIfAbsent(key, k -> createBucket(capacity, refillTokens, refillDuration));
        return bucket.tryConsume(1);
    }

    /**
     * Check if login attempt is allowed (50 per 15 minutes for development)
     */
    public boolean isLoginAllowed(String ipAddress) {
        return tryConsume("login:" + ipAddress, 50, 50, Duration.ofMinutes(15));
    }

    /**
     * Check if OTP generation is allowed (3 per hour)
     */
    public boolean isOtpGenerationAllowed(String email) {
        return tryConsume("otp-gen:" + email, 3, 3, Duration.ofHours(1));
    }

    /**
     * Check if OTP verification is allowed (5 per 15 minutes)
     */
    public boolean isOtpVerificationAllowed(String email) {
        return tryConsume("otp-verify:" + email, 5, 5, Duration.ofMinutes(15));
    }

    /**
     * Check if API request is allowed (100 per minute)
     */
    public boolean isApiRequestAllowed(String ipAddress) {
        return tryConsume("api:" + ipAddress, 100, 100, Duration.ofMinutes(1));
    }

    /**
     * Reset rate limit for a specific key (e.g., after successful login)
     */
    public void reset(String key) {
        buckets.remove(key);
    }

    /**
     * Create a new bucket with specified parameters
     */
    @SuppressWarnings("deprecation")
    private Bucket createBucket(long capacity, long refillTokens, Duration refillDuration) {
        Bandwidth limit = Bandwidth.classic(capacity, Refill.intervally(refillTokens, refillDuration));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Get remaining attempts for a key
     */
    public long getRemainingAttempts(String key, long capacity, long refillTokens, Duration refillDuration) {
        Bucket bucket = buckets.computeIfAbsent(key, k -> createBucket(capacity, refillTokens, refillDuration));
        return bucket.getAvailableTokens();
    }
}
