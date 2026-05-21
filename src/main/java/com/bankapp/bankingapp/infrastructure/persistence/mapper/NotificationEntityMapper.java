package com.bankapp.bankingapp.infrastructure.persistence.mapper;

import com.bankapp.bankingapp.domain.model.Notification;
import com.bankapp.bankingapp.infrastructure.persistence.entity.NotificationEntity;
import com.bankapp.bankingapp.infrastructure.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class NotificationEntityMapper {

    public Notification toDomain(NotificationEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Notification(
                entity.getId(),
                entity.getUser().getId(),
                entity.getType(),
                entity.getTitle(),
                entity.getBody(),
                entity.isRead(),
                entity.getCreatedAt());
    }

    public NotificationEntity toEntity(Notification domain) {
        if (domain == null) {
            return null;
        }

        NotificationEntity entity = new NotificationEntity();
        entity.setId(domain.getId());

        UserEntity user = new UserEntity();
        user.setId(domain.getUserId());
        entity.setUser(user);

        entity.setType(domain.getType());
        entity.setTitle(domain.getTitle());
        entity.setBody(domain.getBody());
        entity.setRead(domain.isRead());
        return entity;
    }
}
