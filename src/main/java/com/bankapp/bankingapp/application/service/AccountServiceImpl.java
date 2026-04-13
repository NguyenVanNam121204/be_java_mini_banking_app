package com.bankapp.bankingapp.application.service;

import com.bankapp.bankingapp.application.dto.request.CreateAccountRequestDto;
import com.bankapp.bankingapp.application.dto.response.AccountResponseDto;
import com.bankapp.bankingapp.application.interfaces.repository.IAccountRepository;
import com.bankapp.bankingapp.application.interfaces.repository.IUserRepository;
import com.bankapp.bankingapp.application.interfaces.service.IAccountService;
import com.bankapp.bankingapp.application.mapper.AccountDtoMapper;
import com.bankapp.bankingapp.domain.model.Account;
import com.bankapp.bankingapp.domain.model.User;
import com.bankapp.bankingapp.domain.model.enums.AccountStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements IAccountService {

    private final IAccountRepository accountRepository;
    private final IUserRepository userRepository;
    private final AccountDtoMapper accountDtoMapper;

    public AccountServiceImpl(IAccountRepository accountRepository, IUserRepository userRepository, AccountDtoMapper accountDtoMapper) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.accountDtoMapper = accountDtoMapper;
    }

    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("No authenticated user found");
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private String generateUniqueAccountNumber() {
        Random random = new Random();
        String accountNumber;
        do {
            // Generate a 10 digit number starting with 1
            long number = 1000000000L + (long)(random.nextDouble() * 8999999999L);
            accountNumber = String.valueOf(number);
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }

    @Override
    @Transactional
    public AccountResponseDto createAccount(CreateAccountRequestDto request) {
        User user = getCurrentAuthenticatedUser();

        // CHỐNG SPAM MỞ THẺ: Kiểm tra số lượng thẻ đang có (ví dụ: Tối đa 3 thẻ/người)
        List<Account> existingAccounts = accountRepository.findByUserId(user.getId());
        if (existingAccounts.size() >= 3) {
            throw new IllegalArgumentException("Bạn đã đạt giới hạn mở thẻ tối đa (3 thẻ) cho một tài khoản trực tuyến. Vui lòng liên hệ quầy giao dịch để được hỗ trợ mở thêm.");
        }

        Account account = new Account(
                null,
                generateUniqueAccountNumber(),
                user.getId(),
                BigDecimal.ZERO,
                AccountStatus.ACTIVE,
                request.getType()
        );

        Account savedAccount = accountRepository.save(account);
        return accountDtoMapper.toAccountResponseDto(savedAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponseDto> getMyAccounts() {
        User user = getCurrentAuthenticatedUser();
        List<Account> accounts = accountRepository.findByUserId(user.getId());
        return accounts.stream()
                .map(accountDtoMapper::toAccountResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponseDto getMyAccountDetails(Long accountId) {
        User user = getCurrentAuthenticatedUser();
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));

        if (!account.getUserId().equals(user.getId())) {
            throw new IllegalArgumentException("Không có quyền truy cập tài khoản này");
        }

        return accountDtoMapper.toAccountResponseDto(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponseDto> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(accountDtoMapper::toAccountResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AccountResponseDto lockAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));
        
        account.lock();
        Account savedAccount = accountRepository.save(account);
        return accountDtoMapper.toAccountResponseDto(savedAccount);
    }

    @Override
    @Transactional
    public AccountResponseDto unlockAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));
        
        account.unlock();
        Account savedAccount = accountRepository.save(account);
        return accountDtoMapper.toAccountResponseDto(savedAccount);
    }

    @Override
    @Transactional
    public AccountResponseDto closeAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));
        
        account.close();
        Account savedAccount = accountRepository.save(account);
        return accountDtoMapper.toAccountResponseDto(savedAccount);
    }
}
