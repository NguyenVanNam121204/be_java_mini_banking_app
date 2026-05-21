package com.bankapp.bankingapp.application.mapper;

import com.bankapp.bankingapp.application.dto.response.NotificationResponseDto;
import com.bankapp.bankingapp.domain.model.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationDtoMapper {

    public NotificationResponseDto toResponseDto(Notification notification) {
        return NotificationResponseDto.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .body(notification.getBody())
                .type(notification.getType())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
