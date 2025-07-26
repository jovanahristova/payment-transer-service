package com.example.payment_transfer_service.dto;

import com.example.payment_transfer_service.entity.AccountType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountCreationRequest {
    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Account name is required")
    @Size(max = 100, message = "Account name must not exceed 100 characters")
    private String accountName;

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    @DecimalMin(value = "0.00", message = "Initial balance must be non-negative")
    @Digits(integer = 10, fraction = 2, message = "Invalid balance format")
    private BigDecimal initialBalance = BigDecimal.ZERO;

    @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    @Pattern(regexp = "[A-Z]{3}", message = "Currency must be 3 uppercase letters")
    private String currency = "USD";
}
