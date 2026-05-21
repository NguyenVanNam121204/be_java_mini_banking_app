package com.bankapp.bankingapp.infrastructure.repository;

import com.bankapp.bankingapp.application.interfaces.repository.INotificationRepository;
import com.bankapp.bankingapp.domain.model.Notification;
import com.bankapp.bankingapp.infrastructure.persistence.jpaRepository.NotificationJpaRepository;
import com.bankapp.bankingapp.infrastructure.persistence.mapper.NotificationEntityMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class NotificationRepositoryImpl implements INotificationRepository {

    private final NotificationJpaRepository notificationJpaRepository;
    private final NotificationEntityMapper notificationEntityMapper;

    public NotificationRepositoryImpl(NotificationJpaRepository notificationJpaRepository,
                                      NotificationEntityMapper notificationEntityMapper) {
        this.notificationJpaRepository = notificationJpaRepository;
        this.notificationEntityMapper = notificationEntityMapper;
    }

    @Override
    public Notification save(Notification notification) {
        return notificationEntityMapper.toDomain(
                notificationJpaRepository.save(notificationEntityMapper.toEntity(notification)));
    }

    @Override
    public Page<Notification> findByUserId(Long userId, Pageable pageable) {
        return notificationJpaRepository.findByUser_IdOrderByCreatedAtDesc(userId, pageable)
                .map(notificationEntityMapper::toDomain);
    }

    @Override
    public Optional<Notification> findByIdAndUserId(Long id, Long userId) {
        return notificationJpaRepository.findByIdAndUser_Id(id, userId)
                .map(notificationEntityMapper::toDomain);
    }

    @Override
    public void markAllAsRead(Long userId) {
        notificationJpaRepository.markAllAsReadByUserId(userId);
    }

    @Override
    public void deleteByIdAndUserId(Long id, Long userId) {
        notificationJpaRepository.deleteByIdAndUser_Id(id, userId);
    }
}
