package com.bankapp.bankingapp.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RefreshToken {

    @Setter
    @EqualsAndHashCode.Include
    private Long id;
    
    private Long userId;
    private String tokenHash;
    private LocalDateTime expiresAt;
    private boolean revoked;
    private LocalDateTime createdAt;

    public RefreshToken(Long userId,
                        String tokenHash,
                        LocalDateTime expiresAt) {

        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.createdAt = LocalDateTime.now();
        this.revoked = false;
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public void revoke() {
        this.revoked = true;
    }

    public boolean isValid() {
        return !revoked && !isExpired();
    }
}