package com.bankapp.bankingapp.application.dto.request;

import com.bankapp.bankingapp.domain.model.enums.AccountType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequestDto {

    @NotNull(message = "Loại tài khoản không được để trống")
    private AccountType type;
}
