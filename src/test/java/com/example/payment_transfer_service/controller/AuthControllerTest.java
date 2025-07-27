//package com.example.payment_transfer_service.controller;
//
//import com.example.payment_transfer_service.BaseTestConfig;
//import com.example.payment_transfer_service.dto.LoginRequest;
//import com.example.payment_transfer_service.dto.UserRegistrationRequest;
//import com.example.payment_transfer_service.entity.User;
//import com.example.payment_transfer_service.entity.UserStatus;
//import com.example.payment_transfer_service.repository.UserRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.DisplayName;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.MediaType;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//public class AuthControllerTest extends BaseTestConfig {
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//
//    @BeforeEach
//    void setUp() {
//        userRepository.deleteAll();
//    }
//
//    @Test
//    @DisplayName("Should register new user successfully")
//    void testSuccessfulRegistration() throws Exception {
//        UserRegistrationRequest request = new UserRegistrationRequest();
//        request.setUsername("newuser");
//        request.setEmail("newuser@example.com");
//        request.setPassword("Password123!");
//        request.setFirstName("New");
//        request.setLastName("User");
//        request.setPhone("+1234567890");
//
//        mockMvc.perform(post("/api/v1/auth/register")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(asJsonString(request)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.id").exists())
//                .andExpect(jsonPath("$.username").value("newuser"))
//                .andExpect(jsonPath("$.email").value("newuser@example.com"))
//                .andExpect(jsonPath("$.firstName").value("New"))
//                .andExpect(jsonPath("$.lastName").value("User"));
//
//        // Verify user was saved
//        assertTrue(userRepository.existsByUsername("newuser"));
//    }
//
//    @Test
//    @DisplayName("Should fail registration with duplicate username")
//    void testDuplicateUsernameRegistration() throws Exception {
//        // Create existing user
//        User existingUser = new User();
//        existingUser.setId("USR001");
//        existingUser.setUsername("existinguser");
//        existingUser.setEmail("existing@example.com");
//        existingUser.setPasswordHash(passwordEncoder.encode("Password123!"));
//        existingUser.setFirstName("Existing");
//        existingUser.setLastName("User");
//        existingUser.setStatus(UserStatus.ACTIVE);
//        userRepository.save(existingUser);
//
//        // Try to register with same username
//        UserRegistrationRequest request = new UserRegistrationRequest();
//        request.setUsername("existinguser");
//        request.setEmail("different@example.com");
//        request.setPassword("Password123!");
//        request.setFirstName("New");
//        request.setLastName("User");
//
//        mockMvc.perform(post("/api/v1/auth/register")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(asJsonString(request)))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.errorCode").value("USERNAME_EXISTS"));
//    }
//
//    @Test
//    @DisplayName("Should validate registration request")
//    void testRegistrationValidation() throws Exception {
//        UserRegistrationRequest request = new UserRegistrationRequest();
//        request.setUsername("nu"); // Too short
//        request.setEmail("invalid-email"); // Invalid email
//        request.setPassword("weak"); // Weak password
//
//        mockMvc.perform(post("/api/v1/auth/register")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(asJsonString(request)))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.errors.username").exists())
//                .andExpect(jsonPath("$.errors.email").exists())
//                .andExpect(jsonPath("$.errors.password").exists());
//    }
//
//    @Test
//    @DisplayName("Should login successfully with valid credentials")
//    void testSuccessfulLogin() throws Exception {
//        // Create user
//        User user = new User();
//        user.setId("USR001");
//        user.setUsername("testuser");
//        user.setEmail("test@example.com");
//        user.setPasswordHash(passwordEncoder.encode("Password123!"));
//        user.setFirstName("Test");
//        user.setLastName("User");
//        user.setStatus(UserStatus.ACTIVE);
//        userRepository.save(user);
//
//        LoginRequest request = new LoginRequest();
//        request.setUsername("testuser");
//        request.setPassword("Password123!");
//
//        mockMvc.perform(post("/api/v1/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(asJsonString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.token").exists())
//                .andExpect(jsonPath("$.tokenType").value("Bearer"))
//                .andExpect(jsonPath("$.userId").value("USR001"))
//                .andExpect(jsonPath("$.username").value("testuser"));
//    }
//
//    @Test
//    @DisplayName("Should fail login with invalid credentials")
//    void testFailedLogin() throws Exception {
//        // Create user
//        User user = new User();
//        user.setId("USR001");
//        user.setUsername("testuser");
//        user.setEmail("test@example.com");
//        user.setPasswordHash(passwordEncoder.encode("Password123!"));
//        user.setFirstName("Test");
//        user.setLastName("User");
//        user.setStatus(UserStatus.ACTIVE);
//        userRepository.save(user);
//
//        LoginRequest request = new LoginRequest();
//        request.setUsername("testuser");
//        request.setPassword("WrongPassword!");
//
//        mockMvc.perform(post("/api/v1/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(asJsonString(request)))
//                .andExpect(status().isUnauthorized());
//    }
//
//    @Test
//    @DisplayName("Should fail login for inactive user")
//    void testLoginInactiveUser() throws Exception {
//        // Create inactive user
//        User user = new User();
//        user.setId("USR001");
//        user.setUsername("inactiveuser");
//        user.setEmail("inactive@example.com");
//        user.setPasswordHash(passwordEncoder.encode("Password123!"));
//        user.setFirstName("Inactive");
//        user.setLastName("User");
//        user.setStatus(UserStatus.SUSPENDED);
//        userRepository.save(user);
//
//        LoginRequest request = new LoginRequest();
//        request.setUsername("inactiveuser");
//        request.setPassword("Password123!");
//
//        mockMvc.perform(post("/api/v1/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(asJsonString(request)))
//                .andExpect(status().isUnauthorized());
//    }
//
//    @Test
//    @DisplayName("Should validate login request")
//    void testLoginValidation() throws Exception {
//        LoginRequest request = new LoginRequest();
//        // Missing required fields
//
//        mockMvc.perform(post("/api/v1/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(asJsonString(request)))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.errors").exists());
//    }
//
//    private boolean assertTrue(boolean condition) {
//        if (!condition) {
//            throw new AssertionError("Expected condition to be true");
//        }
//        return true;
//    }
//}
