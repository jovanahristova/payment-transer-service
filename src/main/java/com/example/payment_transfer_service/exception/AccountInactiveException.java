package com.example.payment_transfer_service.exception;

public class AccountInactiveException extends PaymentException {
    public AccountInactiveException(String accountId, String status) {
        super(String.format("Account %s is %s and cannot process transfers", accountId, status),
                "ACCOUNT_INACTIVE");
    }
}
