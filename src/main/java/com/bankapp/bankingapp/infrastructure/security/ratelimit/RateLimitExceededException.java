package com.bankapp.bankingapp.infrastructure.security.ratelimit;

/**
 * Exception thrown when rate limit is exceeded
 */
public class RateLimitExceededException extends RuntimeException {
    
    private final String limitType;
    private final long retryAfterSeconds;
    
    public RateLimitExceededException(String limitType, long retryAfterSeconds) {
        super(String.format("Rate limit exceeded for %s. Please try again after %d seconds.", 
            limitType, retryAfterSeconds));
        this.limitType = limitType;
        this.retryAfterSeconds = retryAfterSeconds;
    }
    
    public String getLimitType() {
        return limitType;
    }
    
    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
