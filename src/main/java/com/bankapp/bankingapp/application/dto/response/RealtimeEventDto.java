package com.bankapp.bankingapp.application.dto.response;

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
public class RealtimeEventDto {
    private String eventType;
    private Object data;
    private LocalDateTime occurredAt;
}
