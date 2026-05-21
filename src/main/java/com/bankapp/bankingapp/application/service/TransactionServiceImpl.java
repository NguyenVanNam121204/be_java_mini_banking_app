package com.bankapp.bankingapp.application.service;

import com.bankapp.bankingapp.application.dto.request.DepositRequestDto;
import com.bankapp.bankingapp.application.dto.request.TransferRequestDto;
import com.bankapp.bankingapp.application.dto.request.WithdrawRequestDto;
import com.bankapp.bankingapp.application.dto.response.PageResponseDto;
import com.bankapp.bankingapp.application.dto.response.TransactionResponseDto;
import com.bankapp.bankingapp.application.interfaces.repository.IAccountRepository;
import com.bankapp.bankingapp.application.interfaces.repository.ITransactionRepository;
import com.bankapp.bankingapp.application.interfaces.repository.IUserRepository;
import com.bankapp.bankingapp.application.interfaces.service.IAuditService;
import com.bankapp.bankingapp.application.interfaces.service.INotificationService;
import com.bankapp.bankingapp.application.interfaces.service.IRealtimeEventService;
import com.bankapp.bankingapp.application.interfaces.service.ITransactionService;
import com.bankapp.bankingapp.application.mapper.TransactionDtoMapper;
import com.bankapp.bankingapp.domain.model.Account;
import com.bankapp.bankingapp.domain.model.Transaction;
import com.bankapp.bankingapp.domain.model.TransactionEntry;
import com.bankapp.bankingapp.domain.model.User;
import com.bankapp.bankingapp.domain.model.enums.AuditAction;
import com.bankapp.bankingapp.domain.model.enums.EntryType;
import com.bankapp.bankingapp.domain.model.enums.NotificationType;
import com.bankapp.bankingapp.domain.model.enums.TransactionStatus;
import com.bankapp.bankingapp.domain.model.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements ITransactionService {

    private static final BigDecimal HIGH_VALUE_THRESHOLD = new BigDecimal("10000000");

    private final ITransactionRepository transactionRepository;
    private final IAccountRepository accountRepository;
    private final IUserRepository userRepository;
    private final TransactionDtoMapper transactionDtoMapper;
    private final PasswordEncoder passwordEncoder;
    private final IAuditService auditService;
    private final INotificationService notificationService;
    private final IRealtimeEventService realtimeEventService;

    public TransactionServiceImpl(ITransactionRepository transactionRepository,
                                  IAccountRepository accountRepository,
                                  IUserRepository userRepository,
                                  TransactionDtoMapper transactionDtoMapper,
                                  PasswordEncoder passwordEncoder,
                                  IAuditService auditService,
                                  INotificationService notificationService,
                                  IRealtimeEventService realtimeEventService) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.transactionDtoMapper = transactionDtoMapper;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
        this.notificationService = notificationService;
        this.realtimeEventService = realtimeEventService;
    }

    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("Khong tim thay nguoi dung xac thuc");
        }
        return userRepository.findByUsername(authentication.getName())
                .or(() -> userRepository.findByEmail(authentication.getName()))
                .orElseThrow(() -> new UsernameNotFoundException("Nguoi dung khong ton tai"));
    }

    private void validatePin(User user, String rawPin) {
        if (user.isPinLocked()) {
            throw new IllegalArgumentException("Tai khoan dang bi khoa ma PIN do nhap sai qua nhieu lan. Vui long thu lai sau.");
        }

        if (user.getTransactionPinHash() == null) {
            throw new IllegalArgumentException("Ban chua thiet lap ma PIN giao dich. Vui long thiet lap truoc.");
        }

        if (!passwordEncoder.matches(rawPin, user.getTransactionPinHash())) {
            user.increasePinFailedAttempts();
            userRepository.save(user);
            throw new IllegalArgumentException("Ma PIN khong chinh xac.");
        }

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

        Account toAccount = accountRepository.findByAccountNumberForUpdate(request.getAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("Tai khoan nhan khong ton tai"));

        BigDecimal balanceBefore = toAccount.getBalance();
        toAccount.deposit(request.getAmount());

        Account savedAccount = accountRepository.save(toAccount);

        Transaction transaction = new Transaction(
                null, generateReferenceNumber(), TransactionType.DEPOSIT,
                request.getAmount(), null, savedAccount.getId(),
                request.getDescription(), initiatedBy.getUsername()
        );
        transaction.complete();
        Transaction savedTransaction = transactionRepository.save(transaction);

        TransactionEntry entry = new TransactionEntry(
                null, savedTransaction.getId(), savedAccount.getId(), EntryType.CREDIT,
                request.getAmount(), balanceBefore, savedAccount.getBalance()
        );
        transactionRepository.saveEntry(entry);

        auditService.logAction(initiatedBy.getUsername(), AuditAction.DEPOSIT,
                String.format("Nguoi dung [%s] da nap thanh cong %s VND vao tai khoan %s",
                        initiatedBy.getUsername(), request.getAmount(), savedAccount.getAccountNumber()));

        notificationService.notifyUser(
                initiatedBy.getId(),
                NotificationType.TRANSACTION,
                "Biến động số dư",
                "Tài khoản " + maskAccount(savedAccount.getAccountNumber()) + " vừa được cộng "
                        + formatCurrency(request.getAmount()) + " từ giao dịch nạp tiền.");
        publishBalanceUpdatedEvent(initiatedBy.getId(), savedTransaction);
        return transactionDtoMapper.toTransactionResponseDto(savedTransaction);
    }

    @Override
    @Transactional
    public TransactionResponseDto withdraw(WithdrawRequestDto request) {
        User user = getCurrentAuthenticatedUser();
        validatePin(user, request.getPin());

        Account fromAccount = accountRepository.findByAccountNumberForUpdate(request.getAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("Tai khoan khong ton tai"));

        if (!fromAccount.getUserId().equals(user.getId())) {
            throw new IllegalArgumentException("Ban khong co quyen thao tac tren tai khoan nay");
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

        auditService.logAction(user.getUsername(), AuditAction.WITHDRAW,
                String.format("Nguoi dung [%s] da rut thanh cong %s VND tu tai khoan %s",
                        user.getUsername(), request.getAmount(), savedAccount.getAccountNumber()));

        notificationService.notifyUser(
                user.getId(),
                NotificationType.TRANSACTION,
                "Biến động số dư",
                "Tài khoản " + maskAccount(savedAccount.getAccountNumber()) + " vừa bị trừ "
                        + formatCurrency(request.getAmount()) + " từ giao dịch rút tiền.");
        publishBalanceUpdatedEvent(user.getId(), savedTransaction);
        return transactionDtoMapper.toTransactionResponseDto(savedTransaction);
    }

    @Override
    @Transactional
    public TransactionResponseDto transfer(TransferRequestDto request) {
        User user = getCurrentAuthenticatedUser();
        validatePin(user, request.getPin());

        if (request.getFromAccountNumber().equals(request.getToAccountNumber())) {
            throw new IllegalArgumentException("Khong the tu chuyen tien cho chinh minh");
        }

        Account fromAccount = accountRepository.findByAccountNumberForUpdate(request.getFromAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("Tai khoan nguon khong ton tai"));

        if (!fromAccount.getUserId().equals(user.getId())) {
            throw new IllegalArgumentException("Ban khong co quyen thao tac tren tai khoan nguon nay");
        }

        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new IllegalArgumentException("So du khong du de thuc hien giao dich");
        }

        Account toAccount = accountRepository.findByAccountNumberForUpdate(request.getToAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("Tai khoan nhan khong ton tai"));

        boolean isHighValue = request.getAmount().compareTo(HIGH_VALUE_THRESHOLD) >= 0;

        Transaction transaction = new Transaction(
                null, generateReferenceNumber(), TransactionType.TRANSFER,
                request.getAmount(), fromAccount.getId(), toAccount.getId(),
                request.getDescription(), user.getUsername()
        );

        if (isHighValue) {
            Transaction savedTransaction = transactionRepository.save(transaction);

            auditService.logAction(user.getUsername(), AuditAction.TRANSFER_PENDING,
                    String.format("Giao dich gia tri lon (%s VND) dang cho duyet. Ma Ref: %s",
                            request.getAmount(), savedTransaction.getReferenceNumber()));

            notificationService.notifyUser(
                    user.getId(),
                    NotificationType.TRANSACTION,
                    "Giao dịch đang chờ duyệt",
                    "Lệnh chuyển " + formatCurrency(request.getAmount()) + " từ tài khoản "
                            + maskAccount(fromAccount.getAccountNumber()) + " đã được ghi nhận và đang chờ admin phê duyệt.");
            sendAdminEventAfterCommit(
                    "PENDING_TRANSACTION_CREATED",
                    buildPendingTransactionPayload(savedTransaction, fromAccount, toAccount));
            return transactionDtoMapper.toTransactionResponseDto(savedTransaction);
        }

        BigDecimal fromBalanceBefore = fromAccount.getBalance();
        fromAccount.withdraw(request.getAmount());
        Account savedFromAccount = accountRepository.save(fromAccount);

        BigDecimal toBalanceBefore = toAccount.getBalance();
        toAccount.deposit(request.getAmount());
        Account savedToAccount = accountRepository.save(toAccount);

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

        auditService.logAction(user.getUsername(), AuditAction.TRANSFER_SUCCESS,
                String.format("Nguoi dung [%s] da chuyen thanh cong %s VND sang %s",
                        user.getUsername(), request.getAmount(), toAccount.getAccountNumber()));

        notificationService.notifyUser(
                user.getId(),
                NotificationType.TRANSACTION,
                "Chuyển tiền thành công",
                "Bạn đã chuyển " + formatCurrency(request.getAmount()) + " từ tài khoản "
                        + maskAccount(savedFromAccount.getAccountNumber()) + " đến tài khoản "
                        + maskAccount(savedToAccount.getAccountNumber()) + ".");
        notificationService.notifyUser(
                toAccount.getUserId(),
                NotificationType.TRANSACTION,
                "Tiền vào tài khoản",
                "Tài khoản " + maskAccount(savedToAccount.getAccountNumber()) + " vừa nhận "
                        + formatCurrency(request.getAmount()) + " từ giao dịch chuyển khoản.");
        publishBalanceUpdatedEvent(user.getId(), savedTransaction);
        publishBalanceUpdatedEvent(toAccount.getUserId(), savedTransaction);
        return transactionDtoMapper.toTransactionResponseDto(savedTransaction);
    }

    @Override
    @Transactional
    public TransactionResponseDto approveTransaction(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Giao dich khong ton tai"));

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalStateException("Chi co the duyet giao dich dang o trang thai PENDING");
        }

        Account fromAccount = accountRepository.findByIdForUpdate(transaction.getFromAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Tai khoan nguon khong ton tai"));
        Account toAccount = accountRepository.findByIdForUpdate(transaction.getToAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Tai khoan nhan khong ton tai"));

        if (fromAccount.getBalance().compareTo(transaction.getAmount()) < 0) {
            transaction.fail("So du tai khoan nguon khong du tai thoi diem duyet");
            transactionRepository.save(transaction);
            throw new RuntimeException("So du tai khoan nguon khong con du de thuc hien giao dich nay");
        }

        BigDecimal fromBalanceBefore = fromAccount.getBalance();
        fromAccount.withdraw(transaction.getAmount());
        accountRepository.save(fromAccount);

        BigDecimal toBalanceBefore = toAccount.getBalance();
        toAccount.deposit(transaction.getAmount());
        accountRepository.save(toAccount);

        transaction.complete();
        Transaction savedTransaction = transactionRepository.save(transaction);

        transactionRepository.saveEntry(new TransactionEntry(
                null, savedTransaction.getId(), fromAccount.getId(), EntryType.DEBIT,
                transaction.getAmount(), fromBalanceBefore, fromAccount.getBalance()));
        transactionRepository.saveEntry(new TransactionEntry(
                null, savedTransaction.getId(), toAccount.getId(), EntryType.CREDIT,
                transaction.getAmount(), toBalanceBefore, toAccount.getBalance()));

        auditService.logAction(getCurrentUsername(), AuditAction.ADMIN_APPROVE_TRANSACTION,
                "Admin da duyet giao dich ma Ref: " + savedTransaction.getReferenceNumber());

        notificationService.notifyUser(
                fromAccount.getUserId(),
                NotificationType.TRANSACTION,
                "Giao dịch đã được duyệt",
                "Lệnh chuyển " + formatCurrency(transaction.getAmount()) + " từ tài khoản "
                        + maskAccount(fromAccount.getAccountNumber()) + " đã được admin phê duyệt và thực hiện thành công.");
        notificationService.notifyUser(
                toAccount.getUserId(),
                NotificationType.TRANSACTION,
                "Tiền vào tài khoản",
                "Tài khoản " + maskAccount(toAccount.getAccountNumber()) + " vừa nhận "
                        + formatCurrency(transaction.getAmount()) + " từ giao dịch đã được phê duyệt.");
        publishBalanceUpdatedEvent(fromAccount.getUserId(), savedTransaction);
        publishBalanceUpdatedEvent(toAccount.getUserId(), savedTransaction);
        sendAdminEventAfterCommit(
                "PENDING_TRANSACTION_RESOLVED",
                buildResolvedTransactionPayload(savedTransaction, "APPROVED"));
        return transactionDtoMapper.toTransactionResponseDto(savedTransaction);
    }

    @Override
    @Transactional
    public TransactionResponseDto rejectTransaction(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Giao dich khong ton tai"));

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalStateException("Chi co the tu choi giao dich dang o trang thai PENDING");
        }

        transaction.fail("Tu choi boi Admin");
        Transaction savedTransaction = transactionRepository.save(transaction);

        auditService.logAction(getCurrentUsername(), AuditAction.ADMIN_REJECT_TRANSACTION,
                "Admin da tu choi giao dich ma Ref: " + savedTransaction.getReferenceNumber());

        notificationService.notifyUser(
                findUserIdByAccountId(savedTransaction.getFromAccountId()),
                NotificationType.TRANSACTION,
                "Giao dịch bị từ chối",
                "Lệnh chuyển " + formatCurrency(savedTransaction.getAmount()) + " của bạn đã bị admin từ chối. Số dư tài khoản không bị thay đổi.");
        sendAdminEventAfterCommit(
                "PENDING_TRANSACTION_RESOLVED",
                buildResolvedTransactionPayload(savedTransaction, "REJECTED"));
        return transactionDtoMapper.toTransactionResponseDto(savedTransaction);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<TransactionResponseDto> getTransactionHistory(Long accountId, int page, int size, String type) {
        User user = getCurrentAuthenticatedUser();

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Tai khoan khong ton tai"));

        if (!account.getUserId().equals(user.getId())) {
            throw new IllegalArgumentException("Ban khong co quyen xem lich su cua tai khoan nay");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> transactionPage;

        if (type != null && !type.isEmpty() && !type.equalsIgnoreCase("ALL")) {
            transactionPage = transactionRepository.findTransactionsByAccountIdAndType(accountId, type, pageable);
        } else {
            transactionPage = transactionRepository.findTransactionsByAccountId(accountId, pageable);
        }

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

    private Long findUserIdByAccountId(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Tai khoan khong ton tai"))
                .getUserId();
    }

    private String maskAccount(String accountNumber) {
        if (accountNumber == null || accountNumber.length() <= 4) {
            return accountNumber;
        }
        return "*" + accountNumber.substring(accountNumber.length() - 4);
    }

    private String formatCurrency(BigDecimal amount) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format(amount) + " VND";
    }

    private void publishBalanceUpdatedEvent(Long userId, Transaction transaction) {
        sendUserEventAfterCommit(
                userId,
                "ACCOUNT_BALANCE_UPDATED",
                buildUserTransactionPayload(transaction));
    }

    private void sendUserEventAfterCommit(Long userId, String eventType, Object data) {
        runAfterCommit(() -> realtimeEventService.sendUserEvent(userId, eventType, data));
    }

    private void sendAdminEventAfterCommit(String eventType, Object data) {
        runAfterCommit(() -> realtimeEventService.sendAdminEvent(eventType, data));
    }

    private void runAfterCommit(Runnable task) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            task.run();
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                task.run();
            }
        });
    }

    private java.util.Map<String, Object> buildPendingTransactionPayload(Transaction transaction, Account fromAccount, Account toAccount) {
        return java.util.Map.of(
                "transactionId", transaction.getId(),
                "referenceNumber", transaction.getReferenceNumber(),
                "amount", transaction.getAmount(),
                "status", transaction.getStatus().name(),
                "fromAccountNumber", fromAccount.getAccountNumber(),
                "toAccountNumber", toAccount.getAccountNumber(),
                "description", transaction.getDescription() == null ? "" : transaction.getDescription(),
                "createdAt", transaction.getCreatedAt());
    }

    private java.util.Map<String, Object> buildResolvedTransactionPayload(Transaction transaction, String resolution) {
        return java.util.Map.of(
                "transactionId", transaction.getId(),
                "referenceNumber", transaction.getReferenceNumber(),
                "amount", transaction.getAmount(),
                "status", transaction.getStatus().name(),
                "resolution", resolution,
                "completedAt", transaction.getCompletedAt());
    }

    private java.util.Map<String, Object> buildUserTransactionPayload(Transaction transaction) {
        return java.util.Map.of(
                "transactionId", transaction.getId(),
                "referenceNumber", transaction.getReferenceNumber(),
                "status", transaction.getStatus().name(),
                "type", transaction.getType().name());
    }
}
