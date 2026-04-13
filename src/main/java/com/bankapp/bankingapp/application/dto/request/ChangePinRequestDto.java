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
public class ChangePinRequestDto {

    @NotBlank(message = "Mã PIN cũ không được để trống")
    private String currentPin;

    @NotBlank(message = "Mã PIN mới không được để trống")
    @Pattern(regexp = "^\\d{6}$", message = "Mã PIN mới phải là 6 chữ số")
    private String newPin;

    @NotBlank(message = "Xác nhận mã PIN không được để trống")
    private String confirmPin;
}
