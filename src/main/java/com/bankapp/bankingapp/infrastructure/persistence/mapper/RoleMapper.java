package com.bankapp.bankingapp.infrastructure.persistence.mapper;

import com.bankapp.bankingapp.domain.model.Role;
import com.bankapp.bankingapp.infrastructure.persistence.entity.RoleEntity;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class RoleMapper {

    private final PermissionMapper permissionMapper;

    public RoleMapper(PermissionMapper permissionMapper) {
        this.permissionMapper = permissionMapper;
    }

    public Role toDomain(RoleEntity entity) {
        if (entity == null) {
            return null;
        }

        Role role = new Role(entity.getId(), entity.getName());

        // Set permissions
        if (entity.getPermissions() != null) {
            entity.getPermissions().forEach(permissionEntity ->
                role.addPermission(permissionMapper.toDomain(permissionEntity))
            );
        }

        return role;
    }

    public RoleEntity toEntity(Role domain) {
        if (domain == null) {
            return null;
        }

        RoleEntity entity = new RoleEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());

        // Set permissions
        if (domain.getPermissions() != null) {
            entity.setPermissions(
                domain.getPermissions().stream()
                    .map(permissionMapper::toEntity)
                    .collect(Collectors.toSet())
            );
        }

        return entity;
    }
}
