package com.bankapp.bankingapp.common.exception;

import com.bankapp.bankingapp.application.dto.response.ErrorResponseDto;
import com.bankapp.bankingapp.application.validator.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(ValidationException.class)
        public ResponseEntity<ErrorResponseDto> handleValidationException(
                        ValidationException ex,
                        HttpServletRequest request) {
                ErrorResponseDto error = ErrorResponseDto.of(
                                HttpStatus.BAD_REQUEST.value(),
                                "Validation Error",
                                ex.getMessage(),
                                request.getRequestURI());
                return ResponseEntity.badRequest().body(error);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponseDto> handleMethodArgumentNotValid(
                        MethodArgumentNotValidException ex,
                        HttpServletRequest request) {
                List<ErrorResponseDto.ErrorDetail> errors = ex.getBindingResult()
                                .getAllErrors()
                                .stream()
                                .map(error -> {
                                        String fieldName = ((FieldError) error).getField();
                                        String errorMessage = error.getDefaultMessage();
                                        Object rejectedValue = ((FieldError) error).getRejectedValue();
                                        return ErrorResponseDto.ErrorDetail.builder()
                                                        .field(fieldName)
                                                        .message(errorMessage)
                                                        .rejectedValue(rejectedValue)
                                                        .build();
                                })
                                .collect(Collectors.toList());

                ErrorResponseDto errorResponse = ErrorResponseDto.withErrors(
                                HttpStatus.BAD_REQUEST.value(),
                                "Validation Failed",
                                "Input validation failed for one or more fields",
                                request.getRequestURI(),
                                errors);
                return ResponseEntity.badRequest().body(errorResponse);
        }

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ErrorResponseDto> handleBadCredentials(
                        BadCredentialsException ex,
                        HttpServletRequest request) {
                ErrorResponseDto error = ErrorResponseDto.of(
                                HttpStatus.UNAUTHORIZED.value(),
                                "Unauthorized",
                                "Username hoặc password không đúng",
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        @ExceptionHandler(DisabledException.class)
        public ResponseEntity<ErrorResponseDto> handleDisabledException(
                        DisabledException ex,
                        HttpServletRequest request) {
                ErrorResponseDto error = ErrorResponseDto.of(
                                HttpStatus.UNAUTHORIZED.value(),
                                "Account Not Active",
                                "Tài khoản chưa được xác thực. Vui lòng kiểm tra email để lấy mã OTP xác thực.",
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        @ExceptionHandler(UsernameNotFoundException.class)
        public ResponseEntity<ErrorResponseDto> handleUsernameNotFound(
                        UsernameNotFoundException ex,
                        HttpServletRequest request) {
                ErrorResponseDto error = ErrorResponseDto.of(
                                HttpStatus.NOT_FOUND.value(),
                                "Not Found",
                                "User not found",
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErrorResponseDto> handleIllegalArgument(
                        IllegalArgumentException ex,
                        HttpServletRequest request) {
                // Truyền message thực (VD: "Mã PIN không chính xác", "Số dư không đủ"...)
                // để frontend có thể hiển thị lỗi có nghĩa cho người dùng
                String message = ex.getMessage() != null ? ex.getMessage() : "Invalid request";
                ErrorResponseDto error = ErrorResponseDto.of(
                                HttpStatus.BAD_REQUEST.value(),
                                "Bad Request",
                                message,
                                request.getRequestURI());
                return ResponseEntity.badRequest().body(error);
        }

        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<ErrorResponseDto> handleRuntimeException(
                        RuntimeException ex,
                        HttpServletRequest request) {
                ErrorResponseDto error = ErrorResponseDto.of(
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "Internal Server Error",
                                "An error occurred",
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponseDto> handleException(
                        Exception ex,
                        HttpServletRequest request) {
                ErrorResponseDto error = ErrorResponseDto.of(
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "Internal Server Error",
                                "An unexpected error occurred",
                                request.getRequestURI());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
}
