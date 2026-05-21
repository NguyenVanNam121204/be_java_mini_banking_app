package com.bankapp.bankingapp.application.service;

import com.bankapp.bankingapp.application.dto.response.NotificationResponseDto;
import com.bankapp.bankingapp.application.dto.response.PageResponseDto;
import com.bankapp.bankingapp.application.interfaces.repository.INotificationRepository;
import com.bankapp.bankingapp.application.interfaces.repository.IUserRepository;
import com.bankapp.bankingapp.application.interfaces.service.INotificationService;
import com.bankapp.bankingapp.application.interfaces.service.IRealtimeEventService;
import com.bankapp.bankingapp.application.mapper.NotificationDtoMapper;
import com.bankapp.bankingapp.domain.model.Notification;
import com.bankapp.bankingapp.domain.model.User;
import com.bankapp.bankingapp.domain.model.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationServiceImpl implements INotificationService {

    private final INotificationRepository notificationRepository;
    private final IUserRepository userRepository;
    private final NotificationDtoMapper notificationDtoMapper;
    private final IRealtimeEventService realtimeEventService;

    public NotificationServiceImpl(INotificationRepository notificationRepository,
                                   IUserRepository userRepository,
                                   NotificationDtoMapper notificationDtoMapper,
                                   IRealtimeEventService realtimeEventService) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.notificationDtoMapper = notificationDtoMapper;
        this.realtimeEventService = realtimeEventService;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<NotificationResponseDto> getCurrentUserNotifications(int page, int size) {
        User user = getCurrentAuthenticatedUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Notification> notificationPage = notificationRepository.findByUserId(user.getId(), pageable);

        List<NotificationResponseDto> content = notificationPage.getContent().stream()
                .map(notificationDtoMapper::toResponseDto)
                .toList();

        return PageResponseDto.<NotificationResponseDto>builder()
                .content(content)
                .pageNo(notificationPage.getNumber())
                .pageSize(notificationPage.getSize())
                .totalElements(notificationPage.getTotalElements())
                .totalPages(notificationPage.getTotalPages())
                .last(notificationPage.isLast())
                .build();
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        User user = getCurrentAuthenticatedUser();
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        if (!notification.isRead()) {
            notification.markAsRead();
            notificationRepository.save(notification);
        }
    }

    @Override
    @Transactional
    public void markAllAsRead() {
        User user = getCurrentAuthenticatedUser();
        notificationRepository.markAllAsRead(user.getId());
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId) {
        User user = getCurrentAuthenticatedUser();
        notificationRepository.deleteByIdAndUserId(notificationId, user.getId());
    }

    @Override
    @Transactional
    public void notifyUser(Long userId, NotificationType type, String title, String body) {
        Notification savedNotification = notificationRepository.save(new Notification(null, userId, type, title, body));
        NotificationResponseDto response = notificationDtoMapper.toResponseDto(savedNotification);
        runAfterCommit(() -> realtimeEventService.sendUserEvent(
                userId,
                "USER_NOTIFICATION_CREATED",
                response));
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
}
