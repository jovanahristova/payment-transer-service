package com.example.payment_transfer_service.exception;

public class AccountNotFoundException extends PaymentException {
    public AccountNotFoundException(String accountId) {
        super("Account " + accountId + " not found", "ACCOUNT_NOT_FOUND");
    }
}
