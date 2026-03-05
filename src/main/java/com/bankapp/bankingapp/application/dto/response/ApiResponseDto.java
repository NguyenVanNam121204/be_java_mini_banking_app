package com.bankapp.bankingapp.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "API Response chuẩn RESTful")
public class ApiResponseDto<T> {

    private LocalDateTime timestamp;
    private int status;
    private boolean success;
    private String message;
    private T data;

    public static <T> ApiResponseDto<T> success(int status, String message, T data) {
        return ApiResponseDto.<T>builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponseDto<T> success(String message, T data) {
        return success(200, message, data);
    }

    public static <T> ApiResponseDto<T> created(String message, T data) {
        return success(201, message, data);
    }
}
