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
public class WithdrawRequestDto {

    @NotBlank(message = "Số tài khoản rút không được để trống")
    private String accountNumber;

    @NotNull(message = "Số tiền không được để trống")
    @DecimalMin(value = "10000.0", message = "Số tiền rút tối thiểu là 10,000")
    private BigDecimal amount;

    private String description;

    @NotBlank(message = "Mã PIN không được để trống")
    private String pin;
}
