package com.example.payment_transfer_service.dto;

import com.example.payment_transfer_service.entity.TransactionStatus;
import com.example.payment_transfer_service.entity.TransactionType;
import lombok.Data;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class UserTransactionHistory {
    private String id;
    private String sourceAccountId;
    private String sourceAccountName;
    private String destinationAccountId;
    private String destinationAccountName;
    private BigDecimal amount;
    private String currency;
    private TransactionStatus status;
    private TransactionType transactionType;
    private String description;
    private String reference;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private boolean isDebit; // true if money going out, false if coming in

    public String getTransactionDescription() {
        if (isDebit) {
            return "Transfer to " + destinationAccountName;
        } else {
            return "Transfer from " + sourceAccountName;
        }
    }
}
