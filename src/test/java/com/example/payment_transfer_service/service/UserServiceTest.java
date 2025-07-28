package com.example.payment_transfer_service.service;

import com.example.payment_transfer_service.dto.AccountSummary;
import com.example.payment_transfer_service.dto.UserRegistrationRequest;
import com.example.payment_transfer_service.dto.UserResponse;
import com.example.payment_transfer_service.entity.Account;
import com.example.payment_transfer_service.entity.AccountStatus;
import com.example.payment_transfer_service.entity.AccountType;
import com.example.payment_transfer_service.entity.User;
import com.example.payment_transfer_service.entity.UserStatus;
import com.example.payment_transfer_service.exception.PaymentException;
import com.example.payment_transfer_service.repository.AccountRepository;
import com.example.payment_transfer_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserRegistrationRequest registrationRequest;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("USR12345");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashedPassword");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setPhone("+1234567890");
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        testUser.setVersion(0L);

        registrationRequest = new UserRegistrationRequest();
        registrationRequest.setUsername("testuser");
        registrationRequest.setEmail("test@example.com");
        registrationRequest.setPassword("password123");
        registrationRequest.setFirstName("John");
        registrationRequest.setLastName("Doe");
        registrationRequest.setPhone("+1234567890");

        testAccount = new Account();
        testAccount.setId("ACC12345");
        testAccount.setAccountName("Test Account");
        testAccount.setAccountType(AccountType.SAVINGS);
        testAccount.setBalance(new BigDecimal("1000.00"));
        testAccount.setCurrency("USD");
        testAccount.setStatus(AccountStatus.ACTIVE);
        testAccount.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void createUser_Success() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponse result = userService.createUser(registrationRequest);

        assertNotNull(result);
        assertEquals("USR12345", result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("+1234567890", result.getPhone());
        assertEquals(UserStatus.ACTIVE, result.getStatus());

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(accountRepository).findByUserIdOrderByCreatedAtDesc("USR12345");
    }

    @Test
    void createUser_UsernameAlreadyExists() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        PaymentException exception = assertThrows(PaymentException.class, () ->
                userService.createUser(registrationRequest));

        assertEquals("Username already exists", exception.getMessage());
        assertEquals("USERNAME_EXISTS", exception.getErrorCode());

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_EmailAlreadyExists() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        PaymentException exception = assertThrows(PaymentException.class, () ->
                userService.createUser(registrationRequest));

        assertEquals("Email already exists", exception.getMessage());
        assertEquals("EMAIL_EXISTS", exception.getErrorCode());

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_WithAccounts() {
        Account account2 = new Account();
        account2.setId("ACC67890");
        account2.setAccountName("Checking Account");
        account2.setAccountType(AccountType.CHECKING);
        account2.setBalance(new BigDecimal("500.00"));
        account2.setCurrency("USD");
        account2.setStatus(AccountStatus.ACTIVE);
        account2.setCreatedAt(LocalDateTime.now());

        List<Account> accounts = Arrays.asList(testAccount, account2);

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(accountRepository.findByUserIdOrderByCreatedAtDesc("USR12345")).thenReturn(accounts);

        UserResponse result = userService.createUser(registrationRequest);

        assertNotNull(result);
        assertEquals("USR12345", result.getId());
        assertNotNull(result.getAccounts());
        assertEquals(2, result.getAccounts().size());

        AccountSummary firstAccount = result.getAccounts().get(0);
        assertEquals("ACC12345", firstAccount.getId());
        assertEquals("Test Account", firstAccount.getAccountName());
        assertEquals(AccountType.SAVINGS, firstAccount.getAccountType());
        assertEquals(new BigDecimal("1000.00"), firstAccount.getBalance());

        AccountSummary secondAccount = result.getAccounts().get(1);
        assertEquals("ACC67890", secondAccount.getId());
        assertEquals("Checking Account", secondAccount.getAccountName());
        assertEquals(AccountType.CHECKING, secondAccount.getAccountType());
        assertEquals(new BigDecimal("500.00"), secondAccount.getBalance());

        verify(accountRepository).findByUserIdOrderByCreatedAtDesc("USR12345");
    }

    @Test
    void getUserById_Success() {
        when(userRepository.findById("USR12345")).thenReturn(Optional.of(testUser));
        when(accountRepository.findByUserIdOrderByCreatedAtDesc("USR12345"))
                .thenReturn(Arrays.asList(testAccount));

        UserResponse result = userService.getUserById("USR12345");

        assertNotNull(result);
        assertEquals("USR12345", result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("+1234567890", result.getPhone());
        assertEquals(UserStatus.ACTIVE, result.getStatus());
        assertNotNull(result.getAccounts());
        assertEquals(1, result.getAccounts().size());

        verify(userRepository).findById("USR12345");
        verify(accountRepository).findByUserIdOrderByCreatedAtDesc("USR12345");
    }

    @Test
    void getUserById_UserNotFound() {
        when(userRepository.findById("USR12345")).thenReturn(Optional.empty());

        PaymentException exception = assertThrows(PaymentException.class, () ->
                userService.getUserById("USR12345"));

        assertEquals("User not found", exception.getMessage());
        assertEquals("USER_NOT_FOUND", exception.getErrorCode());

        verify(userRepository).findById("USR12345");
        verify(accountRepository, never()).findByUserIdOrderByCreatedAtDesc(anyString());
    }

    @Test
    void getUserById_WithNoAccounts() {
        when(userRepository.findById("USR12345")).thenReturn(Optional.of(testUser));
        when(accountRepository.findByUserIdOrderByCreatedAtDesc("USR12345"))
                .thenReturn(Arrays.asList());

        UserResponse result = userService.getUserById("USR12345");

        assertNotNull(result);
        assertEquals("USR12345", result.getId());
        assertNotNull(result.getAccounts());
        assertEquals(0, result.getAccounts().size());

        verify(userRepository).findById("USR12345");
        verify(accountRepository).findByUserIdOrderByCreatedAtDesc("USR12345");
    }

    @Test
    void createUser_PasswordEncodingVerification() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId("USR12345");
            return user;
        });

        userService.createUser(registrationRequest);

        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(argThat(user ->
                "encodedPassword".equals(user.getPasswordHash())
        ));
    }

    @Test
    void createUser_GeneratesUserId() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            assertNotNull(user.getId());
            assertTrue(user.getId().startsWith("USR"));
            user.setId("USR12345"); // Set for return
            return user;
        });

        UserResponse result = userService.createUser(registrationRequest);

        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }
}
