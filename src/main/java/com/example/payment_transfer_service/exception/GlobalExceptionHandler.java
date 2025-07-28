package com.example.payment_transfer_service.exception;

import com.example.payment_transfer_service.dto.ErrorResponse;
import com.example.payment_transfer_service.dto.ValidationErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientFunds(
            InsufficientFundsException e, HttpServletRequest request) {
        log.warn("Insufficient funds attempt: {} - Path: {}", e.getMessage(), request.getRequestURI());

        ErrorResponse error = createErrorResponse(
                e.getErrorCode(),
                e.getMessage(),
                request.getRequestURI(),
                HttpStatus.PAYMENT_REQUIRED
        );

        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(error);
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotFound(
            AccountNotFoundException e, HttpServletRequest request) {
        log.warn("Account not found: {} - Path: {}", e.getMessage(), request.getRequestURI());

        ErrorResponse error = createErrorResponse(
                e.getErrorCode(),
                e.getMessage(),
                request.getRequestURI(),
                HttpStatus.NOT_FOUND
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(AccountInactiveException.class)
    public ResponseEntity<ErrorResponse> handleAccountInactive(
            AccountInactiveException e, HttpServletRequest request) {
        log.warn("Account inactive: {} - Path: {}", e.getMessage(), request.getRequestURI());

        ErrorResponse error = createErrorResponse(
                e.getErrorCode(),
                e.getMessage(),
                request.getRequestURI(),
                HttpStatus.FORBIDDEN
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(CurrencyMismatchException.class)
    public ResponseEntity<ErrorResponse> handleCurrencyMismatch(
            CurrencyMismatchException e, HttpServletRequest request) {
        log.warn("Currency mismatch: {} - Path: {}", e.getMessage(), request.getRequestURI());

        ErrorResponse error = createErrorResponse(
                e.getErrorCode(),
                e.getMessage(),
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST
        );

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ErrorResponse> handlePaymentException(
            PaymentException e, HttpServletRequest request) {
        log.error("Payment exception: {} - Path: {}", e.getMessage(), request.getRequestURI());

        ErrorResponse error = createErrorResponse(
                e.getErrorCode(),
                e.getMessage(),
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST
        );

        return ResponseEntity.badRequest().body(error);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(
            MethodArgumentNotValidException e, HttpServletRequest request) {
        log.warn("Validation failed on path: {} - Errors: {}",
                request.getRequestURI(), e.getBindingResult().getErrorCount());

        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        ValidationErrorResponse response = ValidationErrorResponse.builder()
                .message("Validation failed")
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .path(request.getRequestURI())
                .status(HttpStatus.BAD_REQUEST.value())
                .build();

        return ResponseEntity.badRequest().body(response);
    }


    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            NoHandlerFoundException e, HttpServletRequest request) {
        log.warn("Endpoint not found: {} {} - Path: {}",
                e.getHttpMethod(), e.getRequestURL(), request.getRequestURI());

        ErrorResponse error = createErrorResponse(
                "ENDPOINT_NOT_FOUND",
                "The requested endpoint was not found",
                request.getRequestURI(),
                HttpStatus.NOT_FOUND
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        log.warn("Type mismatch: {} - Path: {}", e.getMessage(), request.getRequestURI());

        String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
                e.getValue(), e.getName(), e.getRequiredType().getSimpleName());

        ErrorResponse error = createErrorResponse(
                "INVALID_PARAMETER_TYPE",
                message,
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST
        );

        return ResponseEntity.badRequest().body(error);
    }


    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            org.springframework.dao.DataIntegrityViolationException e, HttpServletRequest request) {
        log.error("Data integrity violation: {} - Path: {}", e.getMessage(), request.getRequestURI());

        String userMessage = "A data constraint was violated. Please check your input.";
        if (e.getMessage().contains("foreign key")) {
            userMessage = "Referenced entity does not exist";
        } else if (e.getMessage().contains("unique")) {
            userMessage = "A record with this information already exists";
        }

        ErrorResponse error = createErrorResponse(
                "DATA_INTEGRITY_VIOLATION",
                userMessage,
                request.getRequestURI(),
                HttpStatus.CONFLICT
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(org.springframework.dao.OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLocking(
            org.springframework.dao.OptimisticLockingFailureException e, HttpServletRequest request) {
        log.warn("Optimistic locking failure: {} - Path: {}", e.getMessage(), request.getRequestURI());

        ErrorResponse error = createErrorResponse(
                "CONCURRENT_MODIFICATION",
                "The record was modified by another transaction. Please refresh and try again.",
                request.getRequestURI(),
                HttpStatus.CONFLICT
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }


    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            org.springframework.security.access.AccessDeniedException e, HttpServletRequest request) {
        log.warn("Access denied: {} - Path: {} - User: {}",
                e.getMessage(), request.getRequestURI(), getCurrentUser(request));

        ErrorResponse error = createErrorResponse(
                "ACCESS_DENIED",
                "You do not have permission to access this resource",
                request.getRequestURI(),
                HttpStatus.FORBIDDEN
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception e, HttpServletRequest request) {
        log.error("Unexpected error on path: {} - Error: {}", request.getRequestURI(), e.getMessage(), e);

        ErrorResponse error = createErrorResponse(
                "INTERNAL_ERROR",
                "An unexpected error occurred. Please try again later.",
                request.getRequestURI(),
                HttpStatus.INTERNAL_SERVER_ERROR
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }


    private ErrorResponse createErrorResponse(String errorCode, String message, String path, HttpStatus status) {
        return ErrorResponse.builder()
                .errorCode(errorCode)
                .message(message)
                .timestamp(LocalDateTime.now())
                .path(path)
                .status(status.value())
                .build();
    }

    private String getCurrentUser(HttpServletRequest request) {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}

