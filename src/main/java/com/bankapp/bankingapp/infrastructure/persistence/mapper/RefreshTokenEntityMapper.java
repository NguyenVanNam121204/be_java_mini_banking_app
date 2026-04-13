package com.bankapp.bankingapp.infrastructure.persistence.mapper;

import com.bankapp.bankingapp.domain.model.RefreshToken;
import com.bankapp.bankingapp.infrastructure.persistence.entity.RefreshTokenEntity;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenEntityMapper {

    public RefreshToken toDomain(RefreshTokenEntity entity) {
        if (entity == null) {
            return null;
        }

        RefreshToken token = new RefreshToken(
                entity.getUserId(),
                entity.getTokenHash(),
                entity.getExpiresAt()
        );
        token.setId(entity.getId());
        
        if (entity.isRevoked()) {
            token.revoke();
        }

        return token;
    }

    public RefreshTokenEntity toEntity(RefreshToken domain) {
        if (domain == null) {
            return null;
        }

        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setId(domain.getId());
        entity.setUserId(domain.getUserId());
        entity.setTokenHash(domain.getTokenHash());
        entity.setExpiresAt(domain.getExpiresAt());
        entity.setRevoked(!domain.isValid());
        entity.setCreatedAt(domain.getCreatedAt());

        return entity;
    }
}
