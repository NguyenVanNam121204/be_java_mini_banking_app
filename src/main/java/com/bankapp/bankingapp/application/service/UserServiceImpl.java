package com.bankapp.bankingapp.application.service;

import com.bankapp.bankingapp.application.dto.response.UserResponseDto;
import com.bankapp.bankingapp.application.interfaces.repository.IUserRepository;
import com.bankapp.bankingapp.application.interfaces.repository.IRoleRepository;
import com.bankapp.bankingapp.application.interfaces.service.IAuditService;
import com.bankapp.bankingapp.application.interfaces.service.IEmailService;
import com.bankapp.bankingapp.application.interfaces.service.INotificationService;
import com.bankapp.bankingapp.application.interfaces.service.IUserService;
import com.bankapp.bankingapp.domain.model.enums.AuditAction;
import com.bankapp.bankingapp.application.mapper.UserDtoMapper;
import com.bankapp.bankingapp.domain.model.User;
import com.bankapp.bankingapp.domain.model.Role;
import com.bankapp.bankingapp.domain.model.enums.NotificationType;
import com.bankapp.bankingapp.domain.model.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements IUserService {

    private final IUserRepository userRepository;
    private final IRoleRepository roleRepository;
    private final UserDtoMapper userDtoMapper;
    private final PasswordEncoder passwordEncoder;
    private final IAuditService auditService;
    private final INotificationService notificationService;
    private final IEmailService emailService;

    public UserServiceImpl(IUserRepository userRepository, IRoleRepository roleRepository, UserDtoMapper userDtoMapper, PasswordEncoder passwordEncoder, IAuditService auditService, INotificationService notificationService, IEmailService emailService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userDtoMapper = userDtoMapper;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
        this.notificationService = notificationService;
        this.emailService = emailService;
    }

    @Override
    @Transactional(readOnly = true)
    @Deprecated
    public List<UserResponseDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(userDtoMapper::toUserResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDto> getAllUsersPaginated(@NonNull Pageable pageable, String keyword) {
        return userRepository.findAllPaginated(pageable, keyword)
                .map(userDtoMapper::toUserResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getCurrentUserProfile() {
        User user = getCurrentAuthenticatedUser();
        return userDtoMapper.toUserResponseDto(user);
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

    @Override
    @Transactional
    public void changePassword(com.bankapp.bankingapp.application.dto.request.ChangePasswordRequestDto request) {
        User user = getCurrentAuthenticatedUser();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Mật khẩu cũ không đúng");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Xác nhận mật khẩu không khớp");
        }

        user.forceChangePassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        notificationService.notifyUser(
                user.getId(),
                NotificationType.SECURITY,
                "Mật khẩu vừa được thay đổi",
                "Mật khẩu đăng nhập của bạn đã được cập nhật thành công. Nếu đây không phải thao tác của bạn, vui lòng đổi mật khẩu ngay.");
        emailService.sendPasswordChangedAlert(user.getEmail(), user.getUsername());
    }

    @Override
    @Transactional
    public void setupPin(com.bankapp.bankingapp.application.dto.request.SetupPinRequestDto request) {
        User user = getCurrentAuthenticatedUser();

        if (user.getTransactionPinHash() != null && !user.getTransactionPinHash().isEmpty()) {
            throw new IllegalArgumentException("Mã PIN đã được thiết lập. Vui lòng dùng chức năng Đổi mã PIN.");
        }

        if (!request.getPin().equals(request.getConfirmPin())) {
            throw new IllegalArgumentException("Xác nhận mã PIN không khớp");
        }

        user.setTransactionPin(passwordEncoder.encode(request.getPin()));
        userRepository.save(user);
        notificationService.notifyUser(
                user.getId(),
                NotificationType.SECURITY,
                "Đã thiết lập mã PIN giao dịch",
                "Mã PIN giao dịch đã được thiết lập thành công. Từ bây giờ, bạn có thể sử dụng PIN để xác nhận giao dịch.");
    }

    @Override
    @Transactional
    public void changePin(com.bankapp.bankingapp.application.dto.request.ChangePinRequestDto request) {
        User user = getCurrentAuthenticatedUser();

        if (user.getTransactionPinHash() == null || user.getTransactionPinHash().isEmpty()) {
            throw new IllegalArgumentException("Mã PIN chưa được thiết lập. Vui lòng thiết lập mã PIN trước.");
        }

        if (user.isPinLocked()) {
            throw new IllegalArgumentException("Tài khoản của bạn đang bị khóa mã PIN do nhập sai quá nhiều lần. Vui lòng thử lại sau.");
        }

        if (!passwordEncoder.matches(request.getCurrentPin(), user.getTransactionPinHash())) {
            user.increasePinFailedAttempts();
            userRepository.save(user);
            throw new IllegalArgumentException("Mã PIN cũ không đúng. Bạn đã nhập sai " + user.getPinFailedAttempts() + "/5 lần.");
        }

        if (!request.getNewPin().equals(request.getConfirmPin())) {
            throw new IllegalArgumentException("Xác nhận mã PIN mới không khớp");
        }

        // Đổi pin thành công thì reset lại số lần sai
        user.resetPinFailedAttempts();
        user.setTransactionPin(passwordEncoder.encode(request.getNewPin()));
        userRepository.save(user);
        notificationService.notifyUser(
                user.getId(),
                NotificationType.SECURITY,
                "Mã PIN vừa được thay đổi",
                "Mã PIN giao dịch của bạn đã được cập nhật thành công. Nếu đây không phải thao tác của bạn, vui lòng liên hệ hỗ trợ ngay.");
        emailService.sendPinChangedAlert(user.getEmail(), user.getUsername());
    }

    @Override
    @Transactional
    public boolean verifyPin(String pin) {
        User user = getCurrentAuthenticatedUser();

        if (user.getTransactionPinHash() == null || user.getTransactionPinHash().isEmpty()) {
            throw new IllegalArgumentException("Mã PIN chưa được thiết lập");
        }

        if (user.isPinLocked()) {
            throw new IllegalArgumentException("Tài khoản của bạn đang bị khóa mã PIN do nhập sai quá nhiều lần");
        }

        if (!passwordEncoder.matches(pin, user.getTransactionPinHash())) {
            user.increasePinFailedAttempts();
            userRepository.save(user);
            throw new IllegalArgumentException("Mã PIN hiện tại không đúng. Bạn đã nhập sai " + user.getPinFailedAttempts() + "/5 lần.");
        }

        return true;
    }

    // ADMIN APIs
    
    @Override
    @Transactional
    public UserResponseDto createUserByAdmin(com.bankapp.bankingapp.application.dto.request.AdminCreateUserRequestDto request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username đã tồn tại");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }

        User newUser = new User(
                null,
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                null,
                UserStatus.ACTIVE
        );

        if (request.getRoles() != null) {
            for (String roleName : request.getRoles()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new IllegalArgumentException("Role " + roleName + " không tồn tại"));
                newUser.addRole(role);
            }
        } else {
            // Default Role USER (khop voi ten duoc seed boi DataInitializer)
            Role role = roleRepository.findByName("USER")
                    .orElseThrow(() -> new IllegalArgumentException("Default role USER không tồn tại"));
            newUser.addRole(role);
        }

        User savedUser = userRepository.save(newUser);

        // Ghi Audit Log
        auditService.logAction("ADMIN", AuditAction.USER_CREATED, "Admin tạo mới người dùng: " + savedUser.getUsername());

        return userDtoMapper.toUserResponseDto(savedUser);
    }

    @Override
    @Transactional
    public void lockUser(Long userId) {
        User currentAdmin = getCurrentAuthenticatedUser();
        if (currentAdmin.getId().equals(userId)) {
            throw new IllegalArgumentException("Lỗi: Admin không được phép tự khóa tài khoản của chính mình!");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));
        user.lockUser();
        userRepository.save(user);
        
        // Ghi Audit Log
        auditService.logAction("ADMIN", AuditAction.ACCOUNT_LOCKED, "Admin khóa người dùng: " + user.getUsername());
    }

    @Override
    @Transactional
    public void unlockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        // Ghi Audit Log
        auditService.logAction("ADMIN", AuditAction.ACCOUNT_UNLOCKED, "Admin mở khóa người dùng: " + user.getUsername());
    }

    @Override
    @Transactional
    public void assignRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role " + roleName + " không tồn tại"));
        user.addRole(role);
        userRepository.save(user);

        // Ghi Audit Log
        auditService.logAction("ADMIN", AuditAction.ROLE_ASSIGNED, String.format("Admin gán quyền %s cho người dùng %s", roleName, user.getUsername()));
    }

    @Override
    @Transactional
    public void forceResetPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));
        user.forceChangePassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Ghi Audit Log
        auditService.logAction("ADMIN", AuditAction.ADMIN_FORCE_RESET_PASSWORD, "Admin bắt buộc đổi mật khẩu cho người dùng: " + user.getUsername());
    }
}
