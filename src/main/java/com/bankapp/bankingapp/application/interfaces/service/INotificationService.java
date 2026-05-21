package com.bankapp.bankingapp.application.interfaces.service;

import com.bankapp.bankingapp.application.dto.response.NotificationResponseDto;
import com.bankapp.bankingapp.application.dto.response.PageResponseDto;
import com.bankapp.bankingapp.domain.model.enums.NotificationType;

public interface INotificationService {
    PageResponseDto<NotificationResponseDto> getCurrentUserNotifications(int page, int size);
    void markAsRead(Long notificationId);
    void markAllAsRead();
    void deleteNotification(Long notificationId);
    void notifyUser(Long userId, NotificationType type, String title, String body);
}
