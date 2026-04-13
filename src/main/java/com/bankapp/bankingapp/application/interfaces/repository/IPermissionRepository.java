package com.bankapp.bankingapp.application.interfaces.repository;

import com.bankapp.bankingapp.domain.model.Permission;

import java.util.Optional;

public interface IPermissionRepository {
    
    Permission save(Permission permission);
    
    Optional<Permission> findById(Long id);
    
    Optional<Permission> findByName(String name);
}
