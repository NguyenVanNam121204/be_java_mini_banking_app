package com.bankapp.bankingapp.application.mapper;

import com.bankapp.bankingapp.application.dto.response.AccountResponseDto;
import com.bankapp.bankingapp.domain.model.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountDtoMapper {

    public AccountResponseDto toAccountResponseDto(Account account) {
        if (account == null) {
            return null;
        }

        return AccountResponseDto.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .userId(account.getUserId())
                .balance(account.getBalance())
                .status(account.getStatus())
                .type(account.getType())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
