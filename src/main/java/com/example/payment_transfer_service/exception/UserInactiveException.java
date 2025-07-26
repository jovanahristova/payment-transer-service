package com.example.payment_transfer_service.exception;

public class UserInactiveException extends PaymentException {
    public UserInactiveException(String userId) {
        super("User account is not active", "USER_INACTIVE");
    }
}
