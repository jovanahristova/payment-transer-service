//package com.example.payment_transfer_service.service;
//
//import com.example.payment_transfer_service.dto.UserTransferRequest;
//import com.example.payment_transfer_service.dto.TransferResult;
//import com.example.payment_transfer_service.entity.*;
//import com.example.payment_transfer_service.exception.PaymentException;
//import com.example.payment_transfer_service.repository.*;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.math.BigDecimal;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class PaymentTransferServiceTest {
//
//    @Mock
//    private AccountRepository accountRepository;
//
//    @Mock
//    private TransactionRepository transactionRepository;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private AccountService accountService;
//
//    @InjectMocks
//    private PaymentTransferService paymentTransferService;
//
//    private User testUser;
//    private Account sourceAccount;
//    private Account destinationAccount;
//
//    @BeforeEach
//    void setUp() {
//        testUser = new User();
//        testUser.setId("USR001");
//        testUser.setUsername("testuser");
//        testUser.setStatus(UserStatus.ACTIVE);
//
//        sourceAccount = new Account();
//        sourceAccount.setId("ACC001");
//        sourceAccount.setUserId("USR001");
//        sourceAccount.setBalance(new BigDecimal("1000.00"));
//        sourceAccount.setStatus(AccountStatus.ACTIVE);
//        sourceAccount.setCurrency("USD");
//
//        destinationAccount = new Account();
//        destinationAccount.setId("ACC002");
//        destinationAccount.setUserId("USR001");
//        destinationAccount.setBalance(new BigDecimal("500.00"));
//        destinationAccount.setStatus(AccountStatus.ACTIVE);
//        destinationAccount.setCurrency("USD");
//    }
//
//    @Test
//    @DisplayName("Should successfully transfer funds between accounts")
//    void testSuccessfulTransfer() {
//        // Arrange
//        UserTransferRequest request = new UserTransferRequest();
//        request.setUserId("USR001");
//        request.setSourceAccountId("ACC001");
//        request.setDestinationAccountId("ACC002");
//        request.setAmount(new BigDecimal("100.00"));
//        request.setDescription("Test transfer");
//
//        when(userRepository.findById("USR001")).thenReturn(Optional.of(testUser));
//        when(accountService.validateAccountOwnership("ACC001", "USR001")).thenReturn(true);
//        when(accountService.validateAccountOwnership("ACC002", "USR001")).thenReturn(true);
//        when(transactionRepository.existsByReferenceAndUserId(any(), any())).thenReturn(false);
//        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> {
//            Transaction t = i.getArgument(0);
//            t.setId("TXN001");
//            return t;
//        });
//        when(accountRepository.findByIdForUpdate("ACC001")).thenReturn(Optional.of(sourceAccount));
//        when(accountRepository.findByIdForUpdate("ACC002")).thenReturn(Optional.of(destinationAccount));
//
//        // Act
//        TransferResult result = paymentTransferService.transferFunds(request);
//
//        // Assert
//        assertTrue(result.isSuccess());
//        assertNotNull(result.getTransactionId());
//        assertEquals("Transfer completed successfully", result.getMessage());
//        assertEquals(new BigDecimal("900.00"), sourceAccount.getBalance());
//        assertEquals(new BigDecimal("600.00"), destinationAccount.getBalance());
//
//        verify(accountRepository, times(2)).save(any(Account.class));
//        verify(transactionRepository, times(2)).save(any(Transaction.class));
//    }
//
//    @Test
//    @DisplayName("Should fail when insufficient funds")
//    void testInsufficientFunds() {
//        // Arrange
//        UserTransferRequest request = new UserTransferRequest();
//        request.setUserId("USR001");
//        request.setSourceAccountId("ACC001");
//        request.setDestinationAccountId("ACC002");
//        request.setAmount(new BigDecimal("2000.00")); // More than available
//
//        when(userRepository.findById("USR001")).thenReturn(Optional.of(testUser));
//        when(accountService.validateAccountOwnership("ACC001", "USR001")).thenReturn(true);
//        when(accountService.validateAccountOwnership("ACC002", "USR001")).thenReturn(true);
//        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> {
//            Transaction t = i.getArgument(0);
//            t.setId("TXN002");
//            return t;
//        });
//        when(accountRepository.findByIdForUpdate("ACC001")).thenReturn(Optional.of(sourceAccount));
//        when(accountRepository.findByIdForUpdate("ACC002")).thenReturn(Optional.of(destinationAccount));
//
//        // Act
//        TransferResult result = paymentTransferService.transferFunds(request);
//
//        // Assert
//        assertFalse(result.isSuccess());
//        assertEquals("INSUFFICIENT_FUNDS", result.getErrorCode());
//        verify(accountRepository, never()).save(any(Account.class));
//    }
//
//    @Test
//    @DisplayName("Should fail when user is not active")
//    void testInactiveUser() {
//        // Arrange
//        testUser.setStatus(UserStatus.SUSPENDED);
//
//        UserTransferRequest request = new UserTransferRequest();
//        request.setUserId("USR001");
//        request.setSourceAccountId("ACC001");
//        request.setDestinationAccountId("ACC002");
//        request.setAmount(new BigDecimal("100.00"));
//
//        when(userRepository.findById("USR001")).thenReturn(Optional.of(testUser));
//
//        // Act
//        TransferResult result = paymentTransferService.transferFunds(request);
//
//        // Assert
//        assertFalse(result.isSuccess());
//        assertEquals("USER_INACTIVE", result.getErrorCode());
//        verify(accountRepository, never()).findByIdForUpdate(any());
//    }
//
//    @Test
//    @DisplayName("Should fail when accounts have different currencies")
//    void testCurrencyMismatch() {
//        // Arrange
//        destinationAccount.setCurrency("EUR");
//
//        UserTransferRequest request = new UserTransferRequest();
//        request.setUserId("USR001");
//        request.setSourceAccountId("ACC001");
//        request.setDestinationAccountId("ACC002");
//        request.setAmount(new BigDecimal("100.00"));
//
//        when(userRepository.findById("USR001")).thenReturn(Optional.of(testUser));
//        when(accountService.validateAccountOwnership("ACC001", "USR001")).thenReturn(true);
//        when(accountService.validateAccountOwnership("ACC002", "USR001")).thenReturn(true);
//        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> {
//            Transaction t = i.getArgument(0);
//            t.setId("TXN003");
//            return t;
//        });
//        when(accountRepository.findByIdForUpdate("ACC001")).thenReturn(Optional.of(sourceAccount));
//        when(accountRepository.findByIdForUpdate("ACC002")).thenReturn(Optional.of(destinationAccount));
//
//        // Act
//        TransferResult result = paymentTransferService.transferFunds(request);
//
//        // Assert
//        assertFalse(result.isSuccess());
//        assertEquals("CURRENCY_MISMATCH", result.getErrorCode());
//    }
//
//    @Test
//    @DisplayName("Should get transaction by ID successfully")
//    void testGetTransactionById() {
//        // Arrange
//        Transaction transaction = new Transaction();
//        transaction.setId("TXN001");
//        transaction.setAmount(new BigDecimal("100.00"));
//
//        when(transactionRepository.findById("TXN001")).thenReturn(Optional.of(transaction));
//
//        // Act
//        Transaction result = paymentTransferService.getTransactionById("TXN001");
//
//        // Assert
//        assertNotNull(result);
//        assertEquals("TXN001", result.getId());
//        assertEquals(new BigDecimal("100.00"), result.getAmount());
//    }
//
//    @Test
//    @DisplayName("Should throw exception when transaction not found")
//    void testGetTransactionByIdNotFound() {
//        // Arrange
//        when(transactionRepository.findById("TXN999")).thenReturn(Optional.empty());
//
//        // Act & Assert
//        PaymentException exception = assertThrows(PaymentException.class,
//                () -> paymentTransferService.getTransactionById("TXN999"));
//
//        assertEquals("TRANSACTION_NOT_FOUND", exception.getErrorCode());
//    }
//}
