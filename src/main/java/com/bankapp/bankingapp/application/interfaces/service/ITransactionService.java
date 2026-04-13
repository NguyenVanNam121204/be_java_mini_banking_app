package com.bankapp.bankingapp.application.interfaces.service;

import com.bankapp.bankingapp.application.dto.request.DepositRequestDto;
import com.bankapp.bankingapp.application.dto.request.TransferRequestDto;
import com.bankapp.bankingapp.application.dto.request.WithdrawRequestDto;
import com.bankapp.bankingapp.application.dto.response.TransactionResponseDto;
import com.bankapp.bankingapp.application.dto.response.PageResponseDto;

public interface ITransactionService {
    TransactionResponseDto deposit(DepositRequestDto request);
    TransactionResponseDto withdraw(WithdrawRequestDto request);
    TransactionResponseDto transfer(TransferRequestDto request);
    PageResponseDto<TransactionResponseDto> getTransactionHistory(Long accountId, int page, int size);
}
