package com.bankapp.bankingapp.application.interfaces.service;

import com.bankapp.bankingapp.application.dto.request.CreateAccountRequestDto;
import com.bankapp.bankingapp.application.dto.response.AccountResponseDto;

import java.util.List;

public interface IAccountService {

    // USER APIs
    AccountResponseDto createAccount(CreateAccountRequestDto request);
    
    List<AccountResponseDto> getMyAccounts();
    
    AccountResponseDto getMyAccountDetails(Long accountId);

    // ADMIN APIs
    List<AccountResponseDto> getAllAccounts();
    
    AccountResponseDto lockAccount(Long accountId);
    
    AccountResponseDto unlockAccount(Long accountId);
    
    AccountResponseDto closeAccount(Long accountId);
}
