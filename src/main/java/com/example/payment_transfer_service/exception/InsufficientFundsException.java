package com.example.payment_transfer_service.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends PaymentException {
    public InsufficientFundsException(String accountId, BigDecimal available, BigDecimal requested) {
        super(String.format("Insufficient funds in account %s. Available: %s, Requested: %s",
                accountId, available, requested), "INSUFFICIENT_FUNDS");
    }
}
