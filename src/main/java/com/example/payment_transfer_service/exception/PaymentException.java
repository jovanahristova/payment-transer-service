package com.example.payment_transfer_service.exception;

public class PaymentException extends RuntimeException {
    private final String errorCode;
    private final String userMessage;
    private final String technicalMessage;

    public PaymentException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.userMessage = message;
        this.technicalMessage = message;
    }

    public PaymentException(String userMessage, String technicalMessage, String errorCode) {
        super(technicalMessage);
        this.userMessage = userMessage;
        this.technicalMessage = technicalMessage;
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public String getTechnicalMessage() {
        return technicalMessage;
    }
}
