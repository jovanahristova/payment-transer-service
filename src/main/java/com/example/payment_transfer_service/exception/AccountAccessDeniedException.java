package com.example.payment_transfer_service.exception;

public class AccountAccessDeniedException extends PaymentException {
    public AccountAccessDeniedException(String accountId) {
        super("Access denied to account: " + accountId, "ACCOUNT_ACCESS_DENIED");
    }
}
