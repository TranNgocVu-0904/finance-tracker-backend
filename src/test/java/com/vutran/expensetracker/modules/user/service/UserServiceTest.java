package com.vutran.expensetracker.modules.user.service;

import com.vutran.expensetracker.core.security.JwtUtils;
import com.vutran.expensetracker.modules.category.repository.CategoryRepository;
import com.vutran.expensetracker.modules.user.dto.ChangePasswordRequest;
import com.vutran.expensetracker.modules.user.dto.UserRegisterRequest;
import com.vutran.expensetracker.modules.user.dto.UserResponse;
import com.vutran.expensetracker.modules.user.dto.UserUpdateRequest;
import com.vutran.expensetracker.modules.user.entity.User;
import com.vutran.expensetracker.modules.user.entity.UserProfile;
import com.vutran.expensetracker.modules.user.repository.UserProfileRepository;
import com.vutran.expensetracker.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for the UserService layer.
 * Validates core business operations including user onboarding, authentication, 
 * profile management, and secure password recovery mechanisms.
 */
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    // Mock data access and utility dependencies
    @Mock private UserRepository userRepository;
    @Mock private UserProfileRepository userProfileRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtils jwtUtils;
    @Mock private CategoryRepository categoryRepository;
    @Mock private JavaMailSender mailSender;

    @InjectMocks
    private UserService userService;

    private User mockUser;
    private final String EMAIL = "test@vgu.edu.vn";

    @BeforeEach
    void setUp() {
        // Provision a standard mock user entity for baseline testing
        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setEmail(EMAIL);
        mockUser.setPasswordHash("encoded_password");
        mockUser.setName("Test User");
    }

    // ==========================================
    // --- USER REGISTRATION TESTS ---
    // ==========================================

    /**
     * Verifies the successful registration of a new user, ensuring password hashing 
     * and the generation of default financial categories.
     */
    @Test
    void register_Success() {
        UserRegisterRequest req = new UserRegisterRequest();
        req.setEmail("new@vgu.edu.vn"); 
        req.setPassword("123"); 
        req.setName("New");
        
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        UserResponse res = userService.registerUser(req);
        assertNotNull(res);
        verify(categoryRepository, times(1)).saveAll(anyList()); // Verify default categories creation
    }

    /**
     * Asserts that the system prevents duplicate registrations by throwing an exception 
     * when an existing email is submitted.
     */
    @Test
    void register_Fail_EmailExists() {
        UserRegisterRequest req = new UserRegisterRequest(); 
        req.setEmail(EMAIL);
        
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));
        assertThrows(RuntimeException.class, () -> userService.registerUser(req));
    }

    // ==========================================
    // --- AUTHENTICATION TESTS ---
    // ==========================================

    /**
     * Validates successful authentication, confirming password validation and JWT generation.
     */
    @Test
    void login_Success() {
        UserRegisterRequest req = new UserRegisterRequest(); 
        req.setEmail(EMAIL); 
        req.setPassword("123");
        
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("123", "encoded_password")).thenReturn(true);
        when(jwtUtils.generateTokenFromEmail(EMAIL)).thenReturn("fake-jwt-token");

        assertEquals("fake-jwt-token", userService.login(req));
    }

    /**
     * Ensures authentication fails when invalid credentials are provided.
     */
    @Test
    void login_Fail_WrongPassword() {
        UserRegisterRequest req = new UserRegisterRequest(); 
        req.setEmail(EMAIL); 
        req.setPassword("wrong");
        
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrong", "encoded_password")).thenReturn(false);
        assertThrows(RuntimeException.class, () -> userService.login(req));
    }

    // ==========================================
    // --- PASSWORD MANAGEMENT TESTS ---
    // ==========================================

    /**
     * Verifies that a logged-in user can successfully change their password.
     */
    @Test
    void changePassword_Success() {
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setOldPassword("oldPass"); 
        req.setNewPassword("newPass"); 
        req.setConfirmPassword("newPass");
        
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("oldPass", mockUser.getPasswordHash())).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("new_hash");

        userService.changePassword(EMAIL, req);
        assertEquals("new_hash", mockUser.getPasswordHash());
    }

    /**
     * Asserts failure when the new password and confirmation password do not match.
     */
    @Test
    void changePassword_Fail_PasswordMismatch() {
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setOldPassword("oldPass"); 
        req.setNewPassword("newPass"); 
        req.setConfirmPassword("different");
        
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("oldPass", mockUser.getPasswordHash())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userService.changePassword(EMAIL, req));
    }

    // ==========================================
    // --- PASSWORD RECOVERY TESTS ---
    // ==========================================

    /**
     * Tests the initiation of the password recovery process, ensuring token generation 
     * and email dispatch.
     */
    @Test
    void processForgotPassword_Success() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));
        userService.processForgotPassword(EMAIL);
        
        assertNotNull(mockUser.getResetToken());
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    /**
     * Validates a successful password reset utilizing an active token.
     * Ensures the token is nullified post-use to prevent replay attacks.
     */
    @Test
    void updatePasswordWithToken_Success() {
        String token = "valid-token";
        mockUser.setResetTokenExpiry(LocalDateTime.now().plusMinutes(10)); // Token remains valid
        
        when(userRepository.findByResetToken(token)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.encode("new123")).thenReturn("new_hash");

        userService.updatePasswordWithToken(token, "new123");
        
        // Assert token invalidation logic
        assertNull(mockUser.getResetToken()); 
        assertEquals("new_hash", mockUser.getPasswordHash());
    }

    /**
     * Verifies the system correctly rejects expired reset tokens.
     */
    @Test
    void updatePasswordWithToken_Fail_Expired() {
        String token = "expired-token";
        mockUser.setResetTokenExpiry(LocalDateTime.now().minusMinutes(10)); // Token expired historically
        
        when(userRepository.findByResetToken(token)).thenReturn(Optional.of(mockUser));

        assertThrows(RuntimeException.class, () -> userService.updatePasswordWithToken(token, "new123"));
    }

    // ==========================================
    // --- PROFILE UPDATE TESTS (BRANCH COVERAGE) ---
    // ==========================================

    /**
     * Tests profile updates under standard conditions where all fields are provided.
     */
    @Test
    void updateUserProfile_Success() {
        UserUpdateRequest req = new UserUpdateRequest();
        req.setFirstName("Vu"); 
        req.setLastName("Tran"); 
        req.setPhone("0123"); 
        req.setBio("Dev");
        
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));
        when(userProfileRepository.save(any())).thenReturn(new UserProfile());
        
        UserResponse res = userService.updateUserProfile(EMAIL, req);
        assertEquals("Vu Tran", mockUser.getName());
    }

    /**
     * Evaluates branch coverage: Verifies the service dynamically creates a UserProfile 
     * entity if one does not exist for legacy users.
     */
    @Test
    void updateUserProfile_ProfileIsNull() {
        // Simulate a legacy user without an associated profile record
        mockUser.setProfile(null); 
        
        UserUpdateRequest req = new UserUpdateRequest();
        req.setFirstName("Vu"); // Partial update

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));
        when(userProfileRepository.save(any())).thenReturn(new UserProfile());
        
        userService.updateUserProfile(EMAIL, req);

        // Asserts execution of the instantiation branch: if (profile == null) { ... }
        assertNotNull(mockUser.getProfile(), "The system must auto-generate a new profile if null.");
        assertEquals("Vu", mockUser.getProfile().getFirstName());
    }

    /**
     * Evaluates branch coverage: Ensures empty, null, or whitespace-only inputs 
     * do not overwrite existing valid profile data.
     */
    @Test
    void updateUserProfile_EmptyAndNullFields_ShouldNotUpdate() {
        // Establish existing profile state
        UserProfile oldProfile = new UserProfile();
        oldProfile.setFirstName("OldName");
        oldProfile.setPhone("099999999");
        mockUser.setProfile(oldProfile);

        // Submit a malformed or intentionally empty request
        UserUpdateRequest req = new UserUpdateRequest();
        req.setFirstName("");        // Empty string
        req.setLastName("   ");      // Whitespace
        req.setPhone(null);          // Null value
        req.setBio("");              // Empty string

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));
        when(userProfileRepository.save(any())).thenReturn(oldProfile);

        userService.updateUserProfile(EMAIL, req);

        // Asserts the validation branches correctly block the invalid updates
        assertEquals("OldName", mockUser.getProfile().getFirstName());
        assertEquals("099999999", mockUser.getProfile().getPhone());
    }
}