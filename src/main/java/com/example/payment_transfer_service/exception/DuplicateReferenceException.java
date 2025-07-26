package com.example.payment_transfer_service.exception;

public class DuplicateReferenceException extends PaymentException {
    public DuplicateReferenceException(String reference) {
        super("Reference number already exists: " + reference, "DUPLICATE_REFERENCE");
    }
}
