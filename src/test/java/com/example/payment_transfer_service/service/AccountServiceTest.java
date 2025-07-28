package com.example.payment_transfer_service.service;

import com.example.payment_transfer_service.dto.AccountCreationRequest;
import com.example.payment_transfer_service.dto.AccountSummary;
import com.example.payment_transfer_service.entity.*;
import com.example.payment_transfer_service.exception.PaymentException;
import com.example.payment_transfer_service.repository.AccountRepository;
import com.example.payment_transfer_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AccountService accountService;

    private User testUser;
    private Account testAccount;
    private AccountCreationRequest creationRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user123");
        testUser.setUsername("testuser");
        testUser.setStatus(UserStatus.ACTIVE);

        testAccount = new Account();
        testAccount.setId("ACC12345");
        testAccount.setUser(testUser);
        testAccount.setAccountName("Test Account");
        testAccount.setAccountType(AccountType.SAVINGS);
        testAccount.setBalance(new BigDecimal("1000.00"));
        testAccount.setCurrency("USD");
        testAccount.setStatus(AccountStatus.ACTIVE);
        testAccount.setCreatedAt(LocalDateTime.now());
        testAccount.setVersion(0L);

        creationRequest = new AccountCreationRequest();
        creationRequest.setUserId("user123");
        creationRequest.setAccountName("Test Account");
        creationRequest.setAccountType(AccountType.SAVINGS);
        creationRequest.setInitialBalance(new BigDecimal("1000.00"));
        creationRequest.setCurrency("USD");
    }

    @Test
    void createAccount_Success() {
        when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        AccountSummary result = accountService.createAccount(creationRequest);

        assertNotNull(result);
        assertEquals("ACC12345", result.getId());
        assertEquals("Test Account", result.getAccountName());
        assertEquals(AccountType.SAVINGS, result.getAccountType());
        assertEquals(new BigDecimal("1000.00"), result.getBalance());
        assertEquals("USD", result.getCurrency());
        assertEquals(AccountStatus.ACTIVE, result.getStatus());

        verify(userRepository).findById("user123");
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void createAccount_UserNotFound() {
        when(userRepository.findById("user123")).thenReturn(Optional.empty());

        PaymentException exception = assertThrows(PaymentException.class, () ->
                accountService.createAccount(creationRequest));

        assertEquals("User not found", exception.getMessage());
        assertEquals("USER_NOT_FOUND", exception.getErrorCode());

        verify(userRepository).findById("user123");
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void createAccount_UserInactive() {
        testUser.setStatus(UserStatus.INACTIVE);
        when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));

        PaymentException exception = assertThrows(PaymentException.class, () ->
                accountService.createAccount(creationRequest));

        assertEquals("User is not active", exception.getMessage());
        assertEquals("USER_INACTIVE", exception.getErrorCode());

        verify(userRepository).findById("user123");
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void getUserAccounts_Success() {
        Account account2 = new Account();
        account2.setId("ACC67890");
        account2.setUser(testUser);
        account2.setAccountName("Second Account");
        account2.setAccountType(AccountType.CHECKING);
        account2.setBalance(new BigDecimal("500.00"));
        account2.setCurrency("USD");
        account2.setStatus(AccountStatus.ACTIVE);
        account2.setCreatedAt(LocalDateTime.now());

        List<Account> accounts = Arrays.asList(testAccount, account2);

        when(userRepository.existsById("user123")).thenReturn(true);
        when(accountRepository.findActiveAccountsByUserId("user123")).thenReturn(accounts);

        List<AccountSummary> result = accountService.getUserAccounts("user123");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("ACC12345", result.get(0).getId());
        assertEquals("ACC67890", result.get(1).getId());

        verify(userRepository).existsById("user123");
        verify(accountRepository).findActiveAccountsByUserId("user123");
    }

    @Test
    void getUserAccounts_UserNotFound() {
        when(userRepository.existsById("user123")).thenReturn(false);

        PaymentException exception = assertThrows(PaymentException.class, () ->
                accountService.getUserAccounts("user123"));

        assertEquals("User not found", exception.getMessage());
        assertEquals("USER_NOT_FOUND", exception.getErrorCode());

        verify(userRepository).existsById("user123");
        verify(accountRepository, never()).findActiveAccountsByUserId(anyString());
    }

    @Test
    void getAccountById_Success() {
        when(accountRepository.findByIdAndUserId("ACC12345", "user123"))
                .thenReturn(Optional.of(testAccount));

        AccountSummary result = accountService.getAccountById("ACC12345", "user123");

        assertNotNull(result);
        assertEquals("ACC12345", result.getId());
        assertEquals("Test Account", result.getAccountName());

        verify(accountRepository).findByIdAndUserId("ACC12345", "user123");
    }

    @Test
    void getAccountById_AccountNotFound() {
        when(accountRepository.findByIdAndUserId("ACC12345", "user123"))
                .thenReturn(Optional.empty());

        PaymentException exception = assertThrows(PaymentException.class, () ->
                accountService.getAccountById("ACC12345", "user123"));

        assertEquals("Account not found or access denied", exception.getMessage());
        assertEquals("ACCOUNT_ACCESS_DENIED", exception.getErrorCode());

        verify(accountRepository).findByIdAndUserId("ACC12345", "user123");
    }

    @Test
    void updateAccountStatus_Success() {
        when(accountRepository.findByIdAndUserId("ACC12345", "user123"))
                .thenReturn(Optional.of(testAccount));
        when(accountRepository.save(testAccount)).thenReturn(testAccount);

        AccountSummary result = accountService.updateAccountStatus("ACC12345", "user123", AccountStatus.CLOSED);

        assertNotNull(result);
        assertEquals(AccountStatus.CLOSED, testAccount.getStatus());

        verify(accountRepository).findByIdAndUserId("ACC12345", "user123");
        verify(accountRepository).save(testAccount);
    }

    @Test
    void updateAccountStatus_AccountNotFound() {
        when(accountRepository.findByIdAndUserId("ACC12345", "user123"))
                .thenReturn(Optional.empty());

        PaymentException exception = assertThrows(PaymentException.class, () ->
                accountService.updateAccountStatus("ACC12345", "user123", AccountStatus.CLOSED));

        assertEquals("Account not found or access denied", exception.getMessage());
        assertEquals("ACCOUNT_ACCESS_DENIED", exception.getErrorCode());

        verify(accountRepository).findByIdAndUserId("ACC12345", "user123");
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void validateAccountOwnership_Success() {
        when(accountRepository.findByIdAndUserId("ACC12345", "user123"))
                .thenReturn(Optional.of(testAccount));

        boolean result = accountService.validateAccountOwnership("ACC12345", "user123");

        assertTrue(result);
        verify(accountRepository).findByIdAndUserId("ACC12345", "user123");
    }

    @Test
    void validateAccountOwnership_NoOwnership() {
        when(accountRepository.findByIdAndUserId("ACC12345", "user123"))
                .thenReturn(Optional.empty());

        boolean result = accountService.validateAccountOwnership("ACC12345", "user123");

        assertFalse(result);
        verify(accountRepository).findByIdAndUserId("ACC12345", "user123");
    }
}
