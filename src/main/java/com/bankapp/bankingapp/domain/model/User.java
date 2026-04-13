package com.bankapp.bankingapp.domain.model;

import com.bankapp.bankingapp.domain.model.enums.UserStatus;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

    @Setter
    @EqualsAndHashCode.Include
    private Long id;

    private String username;
    private String email;
    private String passwordHash;
    private String transactionPinHash;

    @Setter
    private int pinFailedAttempts;

    @Setter
    private LocalDateTime pinLockedUntil;

    private UserStatus status;

    // Cho phép service thay đổi status (verifyEmail: PENDING → ACTIVE)
    public void setStatus(UserStatus status) {
        this.status = status;
        updateTimestamp();
    }

    private Set<Role> roles = new HashSet<>();

    @Setter
    private LocalDateTime createdAt;
    @Setter
    private LocalDateTime updatedAt;

    public User(Long id,
            String username,
            String email,
            String passwordHash,
            String transactionPinHash,
            UserStatus status) {

        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.transactionPinHash = transactionPinHash;
        this.status = status;
        this.pinFailedAttempts = 0;
        this.createdAt = LocalDateTime.now();
    }

    // BUSINESS LOGIC

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public void lockUser() {
        this.status = UserStatus.LOCKED;
        updateTimestamp();
    }

    public void disableUser() {
        this.status = UserStatus.DISABLED;
        updateTimestamp();
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public Set<Role> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    public boolean hasRole(String roleName) {
        return roles.stream()
                .anyMatch(r -> r.getName().equals(roleName));
    }

    public boolean hasPermission(String permissionName) {
        return roles.stream()
                .anyMatch(r -> r.hasPermission(permissionName));
    }

    public void increasePinFailedAttempts() {
        this.pinFailedAttempts++;
        if (this.pinFailedAttempts >= 5) {
            this.pinLockedUntil = LocalDateTime.now().plusMinutes(15);
        }
        updateTimestamp();
    }

    public void resetPinFailedAttempts() {
        this.pinFailedAttempts = 0;
        this.pinLockedUntil = null;
        updateTimestamp();
    }

    public boolean isPinLocked() {
        return pinLockedUntil != null && LocalDateTime.now().isBefore(pinLockedUntil);
    }

    public void setTransactionPin(String pinHash) {
        this.transactionPinHash = pinHash;
        updateTimestamp();
    }

    /**
     * Đặt lại mật khẩu (dùng trong forgot password flow)
     * 
     * @param hashedPassword BCrypt hash của mật khẩu mới
     */
    public void forceChangePassword(String hashedPassword) {
        this.passwordHash = hashedPassword;
        updateTimestamp();
    }

    private void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
}