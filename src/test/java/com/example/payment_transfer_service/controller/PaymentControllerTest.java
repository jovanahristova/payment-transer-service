//package com.example.payment_transfer_service.controller;
//
//import com.example.payment_transfer_service.BaseTestConfig;
//import com.example.payment_transfer_service.dto.TransferRequest;
//import com.example.payment_transfer_service.entity.*;
//import com.example.payment_transfer_service.repository.*;
//import com.example.payment_transfer_service.security.JwtUtil;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.DisplayName;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.MediaType;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
//
//import java.math.BigDecimal;
//import java.util.UUID;
//
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//public class PaymentControllerTest extends BaseTestConfig {
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private AccountRepository accountRepository;
//
//    @Autowired
//    private TransactionRepository transactionRepository;
//
//    @Autowired
//    private JwtUtil jwtUtil;
//
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//
//    private String authToken;
//    private User testUser;
//    private Account sourceAccount;
//    private Account destinationAccount;
//
//    @BeforeEach
//    void setUp() {
//        // Clear repositories
//        transactionRepository.deleteAll();
//        accountRepository.deleteAll();
//        userRepository.deleteAll();
//
//        // Create test user
//        testUser = new User();
//        testUser.setId("USR" + UUID.randomUUID().toString().substring(0, 8));
//        testUser.setUsername("testuser");
//        testUser.setEmail("test@example.com");
//        testUser.setPasswordHash(passwordEncoder.encode("Password123!"));
//        testUser.setFirstName("Test");
//        testUser.setLastName("User");
//        testUser.setStatus(UserStatus.ACTIVE);
//        testUser = userRepository.save(testUser);
//
//        // Generate auth token
//        authToken = "Bearer " + jwtUtil.generateJwtToken(testUser.getId());
//
//        // Create test accounts
//        sourceAccount = new Account();
//        sourceAccount.setId("ACC" + UUID.randomUUID().toString().substring(0, 8));
//        sourceAccount.setUser(testUser);
//        sourceAccount.setUserId(testUser.getId());
//        sourceAccount.setAccountName("Test Checking");
//        sourceAccount.setAccountType(AccountType.CHECKING);
//        sourceAccount.setBalance(new BigDecimal("1000.00"));
//        sourceAccount.setCurrency("USD");
//        sourceAccount.setStatus(AccountStatus.ACTIVE);
//        sourceAccount = accountRepository.save(sourceAccount);
//
//        destinationAccount = new Account();
//        destinationAccount.setId("ACC" + UUID.randomUUID().toString().substring(0, 8));
//        destinationAccount.setUser(testUser);
//        destinationAccount.setUserId(testUser.getId());
//        destinationAccount.setAccountName("Test Savings");
//        destinationAccount.setAccountType(AccountType.SAVINGS);
//        destinationAccount.setBalance(new BigDecimal("500.00"));
//        destinationAccount.setCurrency("USD");
//        destinationAccount.setStatus(AccountStatus.ACTIVE);
//        destinationAccount = accountRepository.save(destinationAccount);
//    }
//
//    @Test
//    @DisplayName("Should transfer funds successfully")
//    void testSuccessfulTransfer() throws Exception {
//        TransferRequest request = new TransferRequest();
//        request.setSourceAccountId(sourceAccount.getId());
//        request.setDestinationAccountId(destinationAccount.getId());
//        request.setAmount(new BigDecimal("100.00"));
//        request.setCurrency("USD");
//        request.setDescription("Test transfer");
//
//        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/payments/transfer")
//                        .header("Authorization", authToken)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(asJsonString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.transactionId").exists())
//                .andExpect(jsonPath("$.message").value("Transfer completed successfully"));
//
//        // Verify account balances were updated
//        Account updatedSource = accountRepository.findById(sourceAccount.getId()).orElseThrow();
//        Account updatedDest = accountRepository.findById(destinationAccount.getId()).orElseThrow();
//
//        assertAll(
//                () -> assertEquals(new BigDecimal("900.00"), updatedSource.getBalance()),
//                () -> assertEquals(new BigDecimal("600.00"), updatedDest.getBalance())
//        );
//    }
//
//    @Test
//    @DisplayName("Should fail transfer with insufficient funds")
//    void testInsufficientFundsTransfer() throws Exception {
//        TransferRequest request = new TransferRequest();
//        request.setSourceAccountId(sourceAccount.getId());
//        request.setDestinationAccountId(destinationAccount.getId());
//        request.setAmount(new BigDecimal("2000.00")); // More than available
//        request.setCurrency("USD");
//
//        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/payments/transfer")
//                        .header("Authorization", authToken)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(asJsonString(request)))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.success").value(false))
//                .andExpect(jsonPath("$.errorCode").value("INSUFFICIENT_FUNDS"));
//    }
//
//    @Test
//    @DisplayName("Should require authentication for transfer")
//    void testTransferWithoutAuth() throws Exception {
//        TransferRequest request = new TransferRequest();
//        request.setSourceAccountId(sourceAccount.getId());
//        request.setDestinationAccountId(destinationAccount.getId());
//        request.setAmount(new BigDecimal("100.00"));
//
//        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/payments/transfer")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(asJsonString(request)))
//                .andExpect(status().isUnauthorized());
//    }
//
//    @Test
//    @DisplayName("Should validate transfer request")
//    void testTransferValidation() throws Exception {
//        TransferRequest request = new TransferRequest();
//        // Missing required fields
//
//        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/payments/transfer")
//                        .header("Authorization", authToken)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(asJsonString(request)))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.errors").exists());
//    }
//
//    @Test
//    @DisplayName("Should get transaction by ID")
//    void testGetTransaction() throws Exception {
//        // Create a transaction first
//        Transaction transaction = new Transaction();
//        transaction.setId(UUID.randomUUID().toString());
//        transaction.setUserId(testUser.getId());
//        transaction.setSourceAccountId(sourceAccount.getId());
//        transaction.setDestinationAccountId(destinationAccount.getId());
//        transaction.setAmount(new BigDecimal("50.00"));
//        transaction.setCurrency("USD");
//        transaction.setStatus(TransactionStatus.COMPLETED);
//        transaction.setTransactionType(TransactionType.INTERNAL_TRANSFER);
//        transaction = transactionRepository.save(transaction);
//
//        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/payments/transactions/" + transaction.getId())
//                        .header("Authorization", authToken))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id").value(transaction.getId()))
//                .andExpect(jsonPath("$.amount").value(50.00))
//                .andExpect(jsonPath("$.status").value("COMPLETED"));
//    }
//
//    @Test
//    @DisplayName("Should return 404 for non-existent transaction")
//    void testGetNonExistentTransaction() throws Exception {
//        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/payments/transactions/non-existent-id")
//                        .header("Authorization", authToken))
//                .andExpect(status().isNotFound());
//    }
//
//    private void assertAll(Runnable... assertions) {
//        for (Runnable assertion : assertions) {
//            assertion.run();
//        }
//    }
//
//    private void assertEquals(BigDecimal expected, BigDecimal actual) {
//        if (expected.compareTo(actual) != 0) {
//            throw new AssertionError("Expected: " + expected + " but was: " + actual);
//        }
//    }
//}
