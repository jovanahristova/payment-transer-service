package com.example.payment_transfer_service.dto;

import com.example.payment_transfer_service.entity.AccountStatus;
import com.example.payment_transfer_service.entity.AccountType;
import lombok.Data;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AccountSummary {
    private String id;
    private String accountName;
    private AccountType accountType;
    private BigDecimal balance;
    private String currency;
    private AccountStatus status;
    private LocalDateTime createdAt;
}
