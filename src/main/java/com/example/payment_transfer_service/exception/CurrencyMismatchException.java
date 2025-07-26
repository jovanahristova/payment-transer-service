package com.example.payment_transfer_service.exception;

public class CurrencyMismatchException extends PaymentException {
    public CurrencyMismatchException(String sourceCurrency, String destCurrency) {
        super(String.format("Currency mismatch: source account currency %s, destination account currency %s",
                sourceCurrency, destCurrency), "CURRENCY_MISMATCH");
    }
}
