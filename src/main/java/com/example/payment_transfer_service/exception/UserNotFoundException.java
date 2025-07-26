package com.example.payment_transfer_service.exception;

public class UserNotFoundException extends PaymentException {
    public UserNotFoundException(String userId) {
        super("User not found", "USER_NOT_FOUND");
    }
}
