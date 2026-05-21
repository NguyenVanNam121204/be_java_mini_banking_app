package com.bankapp.bankingapp.application.dto.response;

import com.bankapp.bankingapp.domain.model.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponseDto {
    private Long id;
    private String title;
    private String body;
    private NotificationType type;
    private boolean read;
    private LocalDateTime createdAt;
}
