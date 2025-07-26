package com.example.payment_transfer_service.dto;

import com.example.payment_transfer_service.entity.TransactionType;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransferResult {
    private boolean success;
    private String transactionId;
    private String message;
    private String errorCode;
    private TransactionType transactionType;


    public static TransferResult success(String transactionId, String message) {
        return TransferResult.builder()
                .success(true)
                .transactionId(transactionId)
                .message(message)
                .errorCode(null)
                .build();
    }

    public static TransferResult success(String transactionId, String message, TransactionType type) {
        return TransferResult.builder()
                .success(true)
                .transactionId(transactionId)
                .message(message)
                .errorCode(null)
                .transactionType(type)
                .build();
    }

    public static TransferResult failure(String message, String errorCode) {
        return TransferResult.builder()
                .success(false)
                .transactionId(null)
                .message(message)
                .errorCode(errorCode)
                .build();
    }
}
