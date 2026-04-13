package com.bankapp.bankingapp.application.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequestDto {

    @NotBlank(message = "Số tài khoản nguồn không được để trống")
    private String fromAccountNumber;

    @NotBlank(message = "Số tài khoản đích không được để trống")
    private String toAccountNumber;

    @NotNull(message = "Số tiền không được để trống")
    @DecimalMin(value = "10000.0", message = "Số tiền chuyển tối thiểu là 10,000")
    private BigDecimal amount;

    private String description;

    @NotBlank(message = "Mã PIN không được để trống")
    private String pin;
}
