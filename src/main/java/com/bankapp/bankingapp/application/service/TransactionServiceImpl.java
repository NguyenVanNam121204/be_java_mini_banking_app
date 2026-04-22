package com.bankapp.bankingapp.application.service;

import com.bankapp.bankingapp.application.dto.request.DepositRequestDto;
import com.bankapp.bankingapp.application.dto.request.TransferRequestDto;
import com.bankapp.bankingapp.application.dto.request.WithdrawRequestDto;
import com.bankapp.bankingapp.application.dto.response.TransactionResponseDto;
import com.bankapp.bankingapp.application.interfaces.repository.IAccountRepository;
import com.bankapp.bankingapp.application.interfaces.repository.ITransactionRepository;
import com.bankapp.bankingapp.application.interfaces.repository.IUserRepository;
import com.bankapp.bankingapp.application.interfaces.service.IAuditService;
import com.bankapp.bankingapp.application.interfaces.service.ITransactionService;
import com.bankapp.bankingapp.application.mapper.TransactionDtoMapper;
import com.bankapp.bankingapp.domain.model.Account;
import com.bankapp.bankingapp.domain.model.Transaction;
import com.bankapp.bankingapp.domain.model.TransactionEntry;
import com.bankapp.bankingapp.domain.model.User;
import com.bankapp.bankingapp.domain.model.enums.EntryType;
import com.bankapp.bankingapp.domain.model.enums.TransactionType;
import com.bankapp.bankingapp.domain.model.enums.TransactionStatus;
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
import org.springframework.data.domain.Sort;
import com.bankapp.bankingapp.application.dto.response.PageResponseDto;
@Service
public class TransactionServiceImpl implements ITransactionService {

    private final ITransactionRepository transactionRepository;
    private final IAccountRepository accountRepository;
    private final IUserRepository userRepository;
    private final TransactionDtoMapper transactionDtoMapper;
    private final PasswordEncoder passwordEncoder;
    private final IAuditService auditService;

    public TransactionServiceImpl(ITransactionRepository transactionRepository,
                                  IAccountRepository accountRepository,
                                  IUserRepository userRepository,
                                  TransactionDtoMapper transactionDtoMapper,
                                  PasswordEncoder passwordEncoder,
                                  IAuditService auditService) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.transactionDtoMapper = transactionDtoMapper;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
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

        // Ghi Audit Log
        auditService.logAction(initiatedBy.getUsername(), "DEPOSIT", 
            String.format("Người dùng [%s] đã nạp thành công %s VND vào tài khoản %s", 
                initiatedBy.getUsername(), request.getAmount(), savedAccount.getAccountNumber()));

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

        // Ghi Audit Log
        auditService.logAction(user.getUsername(), "WITHDRAW", 
            String.format("Người dùng [%s] đã rút thành công %s VND từ tài khoản %s", 
                user.getUsername(), request.getAmount(), savedAccount.getAccountNumber()));

        return transactionDtoMapper.toTransactionResponseDto(savedTransaction);
    }

    private static final BigDecimal HIGH_VALUE_THRESHOLD = new BigDecimal("10000000"); // 10 Triệu VND

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

        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new IllegalArgumentException("Số dư không đủ để thực hiện giao dịch");
        }

        Account toAccount = accountRepository.findByAccountNumber(request.getToAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản nhận không tồn tại"));

        // Kiểm tra ngưỡng giá trị lớn
        boolean isHighValue = request.getAmount().compareTo(HIGH_VALUE_THRESHOLD) >= 0;

        Transaction transaction = new Transaction(
                null, generateReferenceNumber(), TransactionType.TRANSFER,
                request.getAmount(), fromAccount.getId(), toAccount.getId(),
                request.getDescription(), user.getUsername()
        );

        if (isHighValue) {
            // Giao dịch giá trị lớn -> Giữ trạng thái PENDING, không chuyển tiền ngay
            Transaction savedTransaction = transactionRepository.save(transaction);
            
            auditService.logAction(user.getUsername(), "HIGH_VALUE_TRANSFER_PENDING", 
                String.format("Giao dịch giá trị lớn (%s VND) đang chờ duyệt. Mã Ref: %s", 
                    request.getAmount(), savedTransaction.getReferenceNumber()));
            
            return transactionDtoMapper.toTransactionResponseDto(savedTransaction);
        }

        // Dưới ngưỡng -> Thực hiện chuyển tiền ngay
        BigDecimal fromBalanceBefore = fromAccount.getBalance();
        fromAccount.withdraw(request.getAmount());
        Account savedFromAccount = accountRepository.save(fromAccount);

        BigDecimal toBalanceBefore = toAccount.getBalance();
        toAccount.deposit(request.getAmount());
        Account savedToAccount = accountRepository.save(toAccount);

        transaction.complete();
        Transaction savedTransaction = transactionRepository.save(transaction);

        // Ghi nhận nợ/có (Entries)
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

        auditService.logAction(user.getUsername(), "TRANSFER_SUCCESS", 
            String.format("Người dùng [%s] đã chuyển thành công %s VND sang %s", 
                user.getUsername(), request.getAmount(), toAccount.getAccountNumber()));

        return transactionDtoMapper.toTransactionResponseDto(savedTransaction);
    }

    @Override
    @Transactional
    public TransactionResponseDto approveTransaction(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Giao dịch không tồn tại"));

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalStateException("Chỉ có thể duyệt giao dịch đang ở trạng thái PENDING");
        }

        Account fromAccount = accountRepository.findById(transaction.getFromAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản nguồn không tồn tại"));
        Account toAccount = accountRepository.findById(transaction.getToAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản nhận không tồn tại"));

        if (fromAccount.getBalance().compareTo(transaction.getAmount()) < 0) {
            transaction.fail("Số dư tài khoản nguồn không đủ tại thời điểm duyệt");
            transactionRepository.save(transaction);
            throw new RuntimeException("Số dư tài khoản nguồn không còn đủ để thực hiện giao dịch này");
        }

        BigDecimal fromBalanceBefore = fromAccount.getBalance();
        fromAccount.withdraw(transaction.getAmount());
        accountRepository.save(fromAccount);

        BigDecimal toBalanceBefore = toAccount.getBalance();
        toAccount.deposit(transaction.getAmount());
        accountRepository.save(toAccount);

        transaction.complete();
        Transaction savedTransaction = transactionRepository.save(transaction);

        // Ghi nhận Entries
        transactionRepository.saveEntry(new TransactionEntry(
                null, savedTransaction.getId(), fromAccount.getId(), EntryType.DEBIT,
                transaction.getAmount(), fromBalanceBefore, fromAccount.getBalance()));
        transactionRepository.saveEntry(new TransactionEntry(
                null, savedTransaction.getId(), toAccount.getId(), EntryType.CREDIT,
                transaction.getAmount(), toBalanceBefore, toAccount.getBalance()));

        auditService.logAction(getCurrentUsername(), "ADMIN_APPROVE_TRANSACTION", 
            "Admin đã duyệt giao dịch mã Ref: " + savedTransaction.getReferenceNumber());

        return transactionDtoMapper.toTransactionResponseDto(savedTransaction);
    }

    @Override
    @Transactional
    public TransactionResponseDto rejectTransaction(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Giao dịch không tồn tại"));

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalStateException("Chỉ có thể từ chối giao dịch đang ở trạng thái PENDING");
        }

        transaction.fail("Từ chối bởi Admin");
        Transaction savedTransaction = transactionRepository.save(transaction);

        auditService.logAction(getCurrentUsername(), "ADMIN_REJECT_TRANSACTION", 
            "Admin đã từ chối giao dịch mã Ref: " + savedTransaction.getReferenceNumber());

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

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<TransactionResponseDto> getAllTransactions(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Transaction> transactionPage = transactionRepository.findAll(pageable);

        List<TransactionResponseDto> content = transactionPage.getContent().stream()
                .map(transactionDtoMapper::toTransactionResponseDto)
                .collect(Collectors.toList());

        return PageResponseDto.<TransactionResponseDto>builder()
                .content(content)
                .pageNo(transactionPage.getNumber())
                .pageSize(transactionPage.getSize())
                .totalElements(transactionPage.getTotalElements())
                .totalPages(transactionPage.getTotalPages())
                .last(transactionPage.isLast())
                .build();
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "system";
        }
        return authentication.getName();
    }
}
