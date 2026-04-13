package com.bankapp.bankingapp.infrastructure.persistence.mapper;

import com.bankapp.bankingapp.domain.model.OtpCode;
import com.bankapp.bankingapp.infrastructure.persistence.entity.OtpCodeEntity;
import org.springframework.stereotype.Component;

@Component
public class OtpCodeEntityMapper {

    public OtpCode toDomain(OtpCodeEntity entity) {
        if (entity == null) {
            return null;
        }

        OtpCode otp = new OtpCode(
                entity.getUserId(),
                entity.getCodeHash(),
                entity.getType(),
                entity.getExpiresAt());
        otp.setId(entity.getId());

        // Replay attempt count
        for (int i = 0; i < entity.getAttemptCount(); i++) {
            otp.incrementAttempt();
        }

        // Replay used state
        if (entity.isUsed()) {
            otp.markAsUsed();
        }

        return otp;
    }

    public OtpCodeEntity toEntity(OtpCode domain) {
        if (domain == null) {
            return null;
        }

        OtpCodeEntity entity = new OtpCodeEntity();
        entity.setId(domain.getId());
        entity.setUserId(domain.getUserId());
        entity.setCodeHash(domain.getCodeHash());
        entity.setType(domain.getType());
        entity.setExpiresAt(domain.getExpiresAt());
        entity.setUsed(domain.isUsed());
        entity.setAttemptCount(domain.getAttemptCount());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUsedAt(domain.getUsedAt());
        return entity;
    }

    public void updateEntity(OtpCode domain, OtpCodeEntity entity) {
        entity.setUsed(domain.isUsed());
        entity.setAttemptCount(domain.getAttemptCount());
        entity.setUsedAt(domain.getUsedAt());
    }
}
