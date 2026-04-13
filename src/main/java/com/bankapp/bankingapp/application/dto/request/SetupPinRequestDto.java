package com.bankapp.bankingapp.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SetupPinRequestDto {

    @NotBlank(message = "Mã PIN không được để trống")
    @Pattern(regexp = "^\\d{6}$", message = "Mã PIN phải là 6 chữ số")
    private String pin;

    @NotBlank(message = "Xác nhận mã PIN không được để trống")
    private String confirmPin;
}
