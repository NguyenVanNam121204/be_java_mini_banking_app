package com.bankapp.bankingapp.application.interfaces.repository;

import com.bankapp.bankingapp.domain.model.Role;

import java.util.Optional;

public interface RoleRepository {
    
    Role save(Role role);
    
    Optional<Role> findById(Long id);
    
    Optional<Role> findByName(String name);
}
