package com.example.payment_transfer_service.service;

import com.example.payment_transfer_service.dto.TransferRequest;
import com.example.payment_transfer_service.dto.UserTransferRequest;
import com.example.payment_transfer_service.dto.TransferResult;
import com.example.payment_transfer_service.entity.*;
import com.example.payment_transfer_service.exception.*;
import com.example.payment_transfer_service.repository.AccountRepository;
import com.example.payment_transfer_service.repository.TransactionRepository;
import com.example.payment_transfer_service.repository.UserRepository;
import com.example.payment_transfer_service.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentTransferServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountService accountService;

    @Mock
    private AuditService auditService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private UserPrincipal userPrincipal;

    @InjectMocks
    private PaymentTransferService paymentTransferService;

    private User testUser;
    private Account sourceAccount;
    private Account destinationAccount;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user123");
        testUser.setStatus(UserStatus.ACTIVE);

        sourceAccount = new Account();
        sourceAccount.setId("acc1");
        sourceAccount.setUserId("user123");
        sourceAccount.setBalance(new BigDecimal("1000.00"));
        sourceAccount.setStatus(AccountStatus.ACTIVE);
        sourceAccount.setCurrency("USD");

        destinationAccount = new Account();
        destinationAccount.setId("acc2");
        destinationAccount.setUserId("user123");
        destinationAccount.setBalance(new BigDecimal("500.00"));
        destinationAccount.setStatus(AccountStatus.ACTIVE);
        destinationAccount.setCurrency("USD");

        testTransaction = new Transaction();
        testTransaction.setId("txn123");
        testTransaction.setUserId("user123");
        testTransaction.setSourceAccountId("acc1");
        testTransaction.setDestinationAccountId("acc2");
        testTransaction.setAmount(new BigDecimal("100.00"));
        testTransaction.setStatus(TransactionStatus.PENDING);
    }

    private void setupSecurityContext() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userPrincipal.getId()).thenReturn("user123");
    }

    @Test
    void transferFunds_UserTransfer_Success() {
        UserTransferRequest request = new UserTransferRequest();
        request.setUserId("user123");
        request.setSourceAccountId("acc1");
        request.setDestinationAccountId("acc2");
        request.setAmount(new BigDecimal("100.00"));

        when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
        when(accountService.validateAccountOwnership("acc1", "user123")).thenReturn(true);
        when(accountService.validateAccountOwnership("acc2", "user123")).thenReturn(true);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
        when(accountRepository.findByIdForUpdate("acc1")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByIdForUpdate("acc2")).thenReturn(Optional.of(destinationAccount));
        when(accountRepository.findById("acc1")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById("acc2")).thenReturn(Optional.of(destinationAccount));

        TransferResult result = paymentTransferService.transferFunds(request);

        assertTrue(result.isSuccess());
        assertEquals("txn123", result.getTransactionId());
        verify(auditService).recordSuccessfulTransfer(any(Transaction.class),
                eq(new BigDecimal("1000.00")), eq(new BigDecimal("900.00")),
                eq(new BigDecimal("500.00")), eq(new BigDecimal("600.00")));
        verify(accountRepository, times(2)).save(any(Account.class));
    }

    @Test
    void transferFunds_UserTransfer_InsufficientFunds() {
        UserTransferRequest request = new UserTransferRequest();
        request.setUserId("user123");
        request.setSourceAccountId("acc1");
        request.setDestinationAccountId("acc2");
        request.setAmount(new BigDecimal("2000.00")); // More than balance

        sourceAccount.setBalance(new BigDecimal("100.00")); // Low balance

        when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
        when(accountService.validateAccountOwnership("acc1", "user123")).thenReturn(true);
        when(accountService.validateAccountOwnership("acc2", "user123")).thenReturn(true);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
        when(accountRepository.findByIdForUpdate("acc1")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByIdForUpdate("acc2")).thenReturn(Optional.of(destinationAccount));

        TransferResult result = paymentTransferService.transferFunds(request);

        assertFalse(result.isSuccess());
        verify(auditService).recordFailedTransfer(
                eq("user123"), eq("acc1"), eq("acc2"),
                eq(new BigDecimal("2000.00")), anyString());
    }

    @Test
    void transferFunds_UserTransfer_UserNotFound() {
        UserTransferRequest request = new UserTransferRequest();
        request.setUserId("user123");
        request.setSourceAccountId("acc1");
        request.setDestinationAccountId("acc2");
        request.setAmount(new BigDecimal("100.00"));

        when(userRepository.findById("user123")).thenReturn(Optional.empty());

        TransferResult result = paymentTransferService.transferFunds(request);

        assertFalse(result.isSuccess());
        assertEquals("USER_NOT_FOUND", result.getErrorCode());
        verify(auditService).recordFailedTransfer(
                eq("user123"), eq("acc1"), eq("acc2"),
                eq(new BigDecimal("100.00")), eq("User not found"));
    }

    @Test
    void transferFunds_UserTransfer_SameAccount() {
        UserTransferRequest request = new UserTransferRequest();
        request.setUserId("user123");
        request.setSourceAccountId("acc1");
        request.setDestinationAccountId("acc1"); // Same account
        request.setAmount(new BigDecimal("100.00"));

        when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
        when(accountService.validateAccountOwnership("acc1", "user123")).thenReturn(true);

        TransferResult result = paymentTransferService.transferFunds(request);

        assertFalse(result.isSuccess());
        assertEquals("SAME_ACCOUNT", result.getErrorCode());
    }

    @Test
    void transferFunds_LegacyTransfer_Success() {
        setupSecurityContext();

        TransferRequest request = new TransferRequest();
        request.setSourceAccountId("acc1");
        request.setDestinationAccountId("acc2");
        request.setAmount(new BigDecimal("100.00"));

        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
        when(accountRepository.findByIdForUpdate("acc1")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByIdForUpdate("acc2")).thenReturn(Optional.of(destinationAccount));

        TransferResult result = paymentTransferService.transferFunds(request);

        assertTrue(result.isSuccess());
        assertEquals("txn123", result.getTransactionId());
        verify(auditService).recordSuccessfulTransfer(any(Transaction.class),
                eq(new BigDecimal("1000.00")), eq(new BigDecimal("900.00")),
                eq(new BigDecimal("500.00")), eq(new BigDecimal("600.00")));
    }

    @Test
    void transferFunds_LegacyTransfer_AccountNotFound() {
        setupSecurityContext();

        TransferRequest request = new TransferRequest();
        request.setSourceAccountId("acc1");
        request.setDestinationAccountId("acc2");
        request.setAmount(new BigDecimal("100.00"));

        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
        when(accountRepository.findByIdForUpdate("acc1")).thenReturn(Optional.empty());

        TransferResult result = paymentTransferService.transferFunds(request);

        assertFalse(result.isSuccess());
        verify(auditService).recordFailedTransfer(
                eq("user123"), eq("acc1"), eq("acc2"),
                eq(new BigDecimal("100.00")), anyString());
    }

    @Test
    void transferFunds_LegacyTransfer_CurrencyMismatch() {
        setupSecurityContext();

        TransferRequest request = new TransferRequest();
        request.setSourceAccountId("acc1");
        request.setDestinationAccountId("acc2");
        request.setAmount(new BigDecimal("100.00"));

        destinationAccount.setCurrency("EUR"); // Different currency

        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
        when(accountRepository.findByIdForUpdate("acc1")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByIdForUpdate("acc2")).thenReturn(Optional.of(destinationAccount));

        TransferResult result = paymentTransferService.transferFunds(request);

        assertFalse(result.isSuccess());
        verify(auditService).recordFailedTransfer(
                eq("user123"), eq("acc1"), eq("acc2"),
                eq(new BigDecimal("100.00")), anyString());
    }

    @Test
    void transferFunds_LegacyTransfer_InactiveAccount() {
        setupSecurityContext();

        TransferRequest request = new TransferRequest();
        request.setSourceAccountId("acc1");
        request.setDestinationAccountId("acc2");
        request.setAmount(new BigDecimal("100.00"));

        sourceAccount.setStatus(AccountStatus.CLOSED);

        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
        when(accountRepository.findByIdForUpdate("acc1")).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByIdForUpdate("acc2")).thenReturn(Optional.of(destinationAccount));

        TransferResult result = paymentTransferService.transferFunds(request);

        assertFalse(result.isSuccess());
        verify(auditService).recordFailedTransfer(
                eq("user123"), eq("acc1"), eq("acc2"),
                eq(new BigDecimal("100.00")), anyString());
    }

    @Test
    void getTransactionById_Success() {
        when(transactionRepository.findById("txn123")).thenReturn(Optional.of(testTransaction));

        Transaction result = paymentTransferService.getTransactionById("txn123");

        assertNotNull(result);
        assertEquals("txn123", result.getId());
    }

    @Test
    void getTransactionById_NotFound() {
        when(transactionRepository.findById("txn123")).thenReturn(Optional.empty());

        assertThrows(PaymentException.class, () ->
                paymentTransferService.getTransactionById("txn123"));
    }
}
