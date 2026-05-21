package com.bankapp.bankingapp.domain.model;

import com.bankapp.bankingapp.domain.model.enums.NotificationType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Notification {

    @Setter
    @EqualsAndHashCode.Include
    private Long id;

    private Long userId;
    private NotificationType type;
    private String title;
    private String body;

    @Setter
    private boolean read;

    @Setter
    private LocalDateTime createdAt;

    public Notification(Long id, Long userId, NotificationType type, String title, String body) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.body = body;
        this.read = false;
        this.createdAt = LocalDateTime.now();
    }

    public Notification(Long id, Long userId, NotificationType type, String title, String body, boolean read, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.body = body;
        this.read = read;
        this.createdAt = createdAt;
    }

    public void markAsRead() {
        this.read = true;
    }
}
