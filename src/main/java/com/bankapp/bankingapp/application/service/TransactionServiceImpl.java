package com.bankapp.bankingapp.application.service;

import com.bankapp.bankingapp.application.dto.request.DepositRequestDto;
import com.bankapp.bankingapp.application.dto.request.TransferRequestDto;
import com.bankapp.bankingapp.application.dto.request.WithdrawRequestDto;
import com.bankapp.bankingapp.application.dto.response.TransactionResponseDto;
import com.bankapp.bankingapp.application.interfaces.repository.IAccountRepository;
import com.bankapp.bankingapp.application.interfaces.repository.ITransactionRepository;
import com.bankapp.bankingapp.application.interfaces.repository.IUserRepository;
import com.bankapp.bankingapp.application.interfaces.service.ITransactionService;
import com.bankapp.bankingapp.application.mapper.TransactionDtoMapper;
import com.bankapp.bankingapp.domain.model.Account;
import com.bankapp.bankingapp.domain.model.Transaction;
import com.bankapp.bankingapp.domain.model.TransactionEntry;
import com.bankapp.bankingapp.domain.model.User;
import com.bankapp.bankingapp.domain.model.enums.EntryType;
import com.bankapp.bankingapp.domain.model.enums.TransactionType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.bankapp.bankingapp.application.dto.response.PageResponseDto;
@Service
public class TransactionServiceImpl implements ITransactionService {

    private final ITransactionRepository transactionRepository;
    private final IAccountRepository accountRepository;
    private final IUserRepository userRepository;
    private final TransactionDtoMapper transactionDtoMapper;
    private final PasswordEncoder passwordEncoder;

    public TransactionServiceImpl(ITransactionRepository transactionRepository,
                                  IAccountRepository accountRepository,
                                  IUserRepository userRepository,
                                  TransactionDtoMapper transactionDtoMapper,
                                  PasswordEncoder passwordEncoder) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.transactionDtoMapper = transactionDtoMapper;
        this.passwordEncoder = passwordEncoder;
    }

    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("Không tìm thấy người dùng xác thực");
        }
        return userRepository.findByUsername(authentication.getName())
                .or(() -> userRepository.findByEmail(authentication.getName()))
                .orElseThrow(() -> new UsernameNotFoundException("Người dùng không tồn tại"));
    }

    private void validatePin(User user, String rawPin) {
        if (user.isPinLocked()) {
            throw new IllegalArgumentException("Tài khoản đang bị khóa mã PIN do nhập sai quá nhiều lần. Vui lòng thử lại sau.");
        }
        
        if (user.getTransactionPinHash() == null) {
            throw new IllegalArgumentException("Bạn chưa thiết lập mã PIN giao dịch. Vui lòng thiết lập trước.");
        }

        if (!passwordEncoder.matches(rawPin, user.getTransactionPinHash())) {
            user.increasePinFailedAttempts();
            userRepository.save(user); // Save to record failed attempts/lockout
            throw new IllegalArgumentException("Mã PIN không chính xác.");
        }
        
        // Save to reset failed attempts if successful
        user.resetPinFailedAttempts();
        userRepository.save(user);
    }

    private String generateReferenceNumber() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 13).toUpperCase();
    }

    @Override
    @Transactional
    public TransactionResponseDto deposit(DepositRequestDto request) {
        User initiatedBy = getCurrentAuthenticatedUser();
        
        Account toAccount = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản nhận không tồn tại"));

        BigDecimal balanceBefore = toAccount.getBalance();
        toAccount.deposit(request.getAmount());

        Account savedAccount = accountRepository.save(toAccount);

        Transaction transaction = new Transaction(
                null, generateReferenceNumber(), TransactionType.DEPOSIT,
                request.getAmount(), null, savedAccount.getId(),
                request.getDescription(), initiatedBy.getUsername()
        );
        transaction.complete(); // auto complete for simple deposit
        Transaction savedTransaction = transactionRepository.save(transaction);

        TransactionEntry entry = new TransactionEntry(
                null, savedTransaction.getId(), savedAccount.getId(), EntryType.CREDIT,
                request.getAmount(), balanceBefore, savedAccount.getBalance()
        );
        transactionRepository.saveEntry(entry);

        return transactionDtoMapper.toTransactionResponseDto(savedTransaction);
    }

    @Override
    @Transactional
    public TransactionResponseDto withdraw(WithdrawRequestDto request) {
        User user = getCurrentAuthenticatedUser();
        validatePin(user, request.getPin());

        Account fromAccount = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản không tồn tại"));

        if (!fromAccount.getUserId().equals(user.getId())) {
            throw new IllegalArgumentException("Bạn không có quyền thao tác trên tài khoản này");
        }

        BigDecimal balanceBefore = fromAccount.getBalance();
        fromAccount.withdraw(request.getAmount());

        Account savedAccount = accountRepository.save(fromAccount);

        Transaction transaction = new Transaction(
                null, generateReferenceNumber(), TransactionType.WITHDRAW,
                request.getAmount(), savedAccount.getId(), null,
                request.getDescription(), user.getUsername()
        );
        transaction.complete();
        Transaction savedTransaction = transactionRepository.save(transaction);

        TransactionEntry entry = new TransactionEntry(
                null, savedTransaction.getId(), savedAccount.getId(), EntryType.DEBIT,
                request.getAmount(), balanceBefore, savedAccount.getBalance()
        );
        transactionRepository.saveEntry(entry);

        return transactionDtoMapper.toTransactionResponseDto(savedTransaction);
    }

    @Override
    @Transactional
    public TransactionResponseDto transfer(TransferRequestDto request) {
        User user = getCurrentAuthenticatedUser();
        validatePin(user, request.getPin());

        if (request.getFromAccountNumber().equals(request.getToAccountNumber())) {
            throw new IllegalArgumentException("Không thể tự chuyển tiền cho chính mình");
        }

        Account fromAccount = accountRepository.findByAccountNumber(request.getFromAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản nguồn không tồn tại"));

        if (!fromAccount.getUserId().equals(user.getId())) {
            throw new IllegalArgumentException("Bạn không có quyền thao tác trên tài khoản nguồn này");
        }

        Account toAccount = accountRepository.findByAccountNumber(request.getToAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản nhận không tồn tại"));

        BigDecimal fromBalanceBefore = fromAccount.getBalance();
        fromAccount.withdraw(request.getAmount());
        Account savedFromAccount = accountRepository.save(fromAccount);

        BigDecimal toBalanceBefore = toAccount.getBalance();
        toAccount.deposit(request.getAmount());
        Account savedToAccount = accountRepository.save(toAccount);

        Transaction transaction = new Transaction(
                null, generateReferenceNumber(), TransactionType.TRANSFER,
                request.getAmount(), savedFromAccount.getId(), savedToAccount.getId(),
                request.getDescription(), user.getUsername()
        );
        transaction.complete();
        Transaction savedTransaction = transactionRepository.save(transaction);

        TransactionEntry debitEntry = new TransactionEntry(
                null, savedTransaction.getId(), savedFromAccount.getId(), EntryType.DEBIT,
                request.getAmount(), fromBalanceBefore, savedFromAccount.getBalance()
        );
        transactionRepository.saveEntry(debitEntry);

        TransactionEntry creditEntry = new TransactionEntry(
                null, savedTransaction.getId(), savedToAccount.getId(), EntryType.CREDIT,
                request.getAmount(), toBalanceBefore, savedToAccount.getBalance()
        );
        transactionRepository.saveEntry(creditEntry);

        return transactionDtoMapper.toTransactionResponseDto(savedTransaction);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<TransactionResponseDto> getTransactionHistory(Long accountId, int page, int size) {
        User user = getCurrentAuthenticatedUser();
        
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản không tồn tại"));

        // Phân quyền: Đảm bảo tài khoản này thuộc về user đang request
        if (!account.getUserId().equals(user.getId())) {
            throw new IllegalArgumentException("Bạn không có quyền xem lịch sử của tài khoản này");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> transactionPage = transactionRepository.findTransactionsByAccountId(accountId, pageable);

        List<TransactionResponseDto> dtos = transactionPage.getContent().stream()
                .map(transactionDtoMapper::toTransactionResponseDto)
                .collect(Collectors.toList());

        return PageResponseDto.<TransactionResponseDto>builder()
                .content(dtos)
                .pageNo(transactionPage.getNumber())
                .pageSize(transactionPage.getSize())
                .totalElements(transactionPage.getTotalElements())
                .totalPages(transactionPage.getTotalPages())
                .last(transactionPage.isLast())
                .build();
    }
}
