package com.bankapp.bankingapp.application.interfaces.repository;

import com.bankapp.bankingapp.domain.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface INotificationRepository {
    Notification save(Notification notification);
    Page<Notification> findByUserId(Long userId, Pageable pageable);
    Optional<Notification> findByIdAndUserId(Long id, Long userId);
    void markAllAsRead(Long userId);
    void deleteByIdAndUserId(Long id, Long userId);
}
