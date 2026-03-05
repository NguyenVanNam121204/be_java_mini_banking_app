package com.bankapp.bankingapp.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Permission {

    @Setter
    @EqualsAndHashCode.Include
    private Long id;
    
    private String name;
    private String description;

    public Permission(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
}