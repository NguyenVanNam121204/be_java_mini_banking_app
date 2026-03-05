package com.bankapp.bankingapp.infrastructure.persistence.mapper;

import com.bankapp.bankingapp.domain.model.Permission;
import com.bankapp.bankingapp.infrastructure.persistence.entity.PermissionEntity;
import org.springframework.stereotype.Component;

@Component
public class PermissionMapper {

    public Permission toDomain(PermissionEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Permission(
                entity.getId(),
                entity.getName(),
                entity.getDescription()
        );
    }

    public PermissionEntity toEntity(Permission domain) {
        if (domain == null) {
            return null;
        }

        PermissionEntity entity = new PermissionEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());

        return entity;
    }
}
