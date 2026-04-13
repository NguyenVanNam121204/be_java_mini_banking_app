package com.bankapp.bankingapp.application.service;

import com.bankapp.bankingapp.application.dto.request.DepositRequestDto;
import com.bankapp.bankingapp.application.dto.response.TransactionResponseDto;
import com.bankapp.bankingapp.application.interfaces.repository.IAccountRepository;
import com.bankapp.bankingapp.application.interfaces.repository.ITransactionRepository;
import com.bankapp.bankingapp.application.interfaces.repository.IUserRepository;
import com.bankapp.bankingapp.application.mapper.TransactionDtoMapper;
import com.bankapp.bankingapp.domain.model.Account;
import com.bankapp.bankingapp.domain.model.Transaction;
import com.bankapp.bankingapp.domain.model.User;
import com.bankapp.bankingapp.domain.model.enums.AccountStatus;
import com.bankapp.bankingapp.domain.model.enums.AccountType;
import com.bankapp.bankingapp.domain.model.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Test for TransactionServiceImpl.
 * This addresses Issue 1 (No Unit/Integration Tests) by providing coverage
 * for the core financial logic of the application.
 */
class TransactionServiceImplTest {

    @Mock
    private ITransactionRepository transactionRepository;

    @Mock
    private IAccountRepository accountRepository;

    @Mock
    private IUserRepository userRepository;

    @Mock
    private TransactionDtoMapper transactionDtoMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
    }

    @Test
    void deposit_ShouldSucceed_WhenAccountExists() {
        // Arrange
        String username = "testuser";
        String accountNumber = "123456789";
        BigDecimal depositAmount = new BigDecimal("100.00");
        BigDecimal initialBalance = new BigDecimal("500.00");

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);

        User user = new User(1L, username, "test@email.com", "hash", null, UserStatus.ACTIVE);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        Account account = new Account(1L, accountNumber, 1L, initialBalance, AccountStatus.ACTIVE, AccountType.SAVINGS);
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        TransactionResponseDto expectedDto = TransactionResponseDto.builder()
                .amount(depositAmount)
                .referenceNumber("TX123")
                .build();
        when(transactionDtoMapper.toTransactionResponseDto(any(Transaction.class))).thenReturn(expectedDto);

        DepositRequestDto request = new DepositRequestDto(accountNumber, depositAmount, "Testing deposit");

        // Act
        TransactionResponseDto result = transactionService.deposit(request);

        // Assert
        assertNotNull(result);
        assertEquals(depositAmount, result.getAmount());
        assertEquals(new BigDecimal("600.00"), account.getBalance()); 
        
        verify(accountRepository).save(any(Account.class));
        verify(transactionRepository).save(any(Transaction.class));
        verify(transactionRepository).saveEntry(any());
    }

    @Test
    void deposit_ShouldThrowException_WhenAccountNotFound() {
        // Arrange
        String username = "testuser";
        String accountNumber = "invalid_acc";
        
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(username);

        User user = new User(1L, username, "test@email.com", "hash", null, UserStatus.ACTIVE);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.empty());

        DepositRequestDto request = new DepositRequestDto(accountNumber, new BigDecimal("100.00"), "test");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> transactionService.deposit(request));
    }
}
