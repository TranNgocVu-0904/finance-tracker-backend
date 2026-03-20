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

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

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
        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setEmail(EMAIL);
        mockUser.setPasswordHash("encoded_password");
        mockUser.setName("Test User");
    }

    // --- TEST REGISTER ---
    @Test
    void register_Success() {
        UserRegisterRequest req = new UserRegisterRequest();
        req.setEmail("new@vgu.edu.vn"); req.setPassword("123"); req.setName("New");
        
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        UserResponse res = userService.registerUser(req);
        assertNotNull(res);
        verify(categoryRepository, times(1)).saveAll(anyList());
    }

    @Test
    void register_Fail_EmailExists() {
        UserRegisterRequest req = new UserRegisterRequest(); req.setEmail(EMAIL);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));
        assertThrows(RuntimeException.class, () -> userService.registerUser(req));
    }

    // --- TEST LOGIN ---
    @Test
    void login_Success() {
        UserRegisterRequest req = new UserRegisterRequest(); req.setEmail(EMAIL); req.setPassword("123");
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("123", "encoded_password")).thenReturn(true);
        when(jwtUtils.generateTokenFromEmail(EMAIL)).thenReturn("fake-jwt-token");

        assertEquals("fake-jwt-token", userService.login(req));
    }

    @Test
    void login_Fail_WrongPassword() {
        UserRegisterRequest req = new UserRegisterRequest(); req.setEmail(EMAIL); req.setPassword("wrong");
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrong", "encoded_password")).thenReturn(false);
        assertThrows(RuntimeException.class, () -> userService.login(req));
    }

    // --- TEST CHANGE PASSWORD ---
    @Test
    void changePassword_Success() {
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setOldPassword("oldPass"); req.setNewPassword("newPass"); req.setConfirmPassword("newPass");
        
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("oldPass", mockUser.getPasswordHash())).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("new_hash");

        userService.changePassword(EMAIL, req);
        assertEquals("new_hash", mockUser.getPasswordHash());
    }

    @Test
    void changePassword_Fail_PasswordMismatch() {
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setOldPassword("oldPass"); req.setNewPassword("newPass"); req.setConfirmPassword("different");
        
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("oldPass", mockUser.getPasswordHash())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userService.changePassword(EMAIL, req));
    }

    // --- TEST FORGOT PASSWORD ---
    @Test
    void processForgotPassword_Success() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));
        userService.processForgotPassword(EMAIL);
        assertNotNull(mockUser.getResetToken());
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    // --- TEST RESET PASSWORD ---
    @Test
    void updatePasswordWithToken_Success() {
        String token = "valid-token";
        mockUser.setResetTokenExpiry(LocalDateTime.now().plusMinutes(10)); // Chưa hết hạn
        when(userRepository.findByResetToken(token)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.encode("new123")).thenReturn("new_hash");

        userService.updatePasswordWithToken(token, "new123");
        assertNull(mockUser.getResetToken()); // Xóa token sau khi dùng
        assertEquals("new_hash", mockUser.getPasswordHash());
    }

    @Test
    void updatePasswordWithToken_Fail_Expired() {
        String token = "expired-token";
        mockUser.setResetTokenExpiry(LocalDateTime.now().minusMinutes(10)); // Đã hết hạn trong quá khứ
        when(userRepository.findByResetToken(token)).thenReturn(Optional.of(mockUser));

        assertThrows(RuntimeException.class, () -> userService.updatePasswordWithToken(token, "new123"));
    }

    // --- TEST UPDATE PROFILE ---
    @Test
    void updateUserProfile_Success() {
        UserUpdateRequest req = new UserUpdateRequest();
        req.setFirstName("Vu"); req.setLastName("Tran"); req.setPhone("0123"); req.setBio("Dev");
        
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));
        when(userProfileRepository.save(any())).thenReturn(new UserProfile());
        
        UserResponse res = userService.updateUserProfile(EMAIL, req);
        assertEquals("Vu Tran", mockUser.getName());
    }
    // --- BỔ SUNG TEST ĐỂ CÀY ĐIỂM BRANCH COVERAGE ---

    @Test
    void updateUserProfile_ProfileIsNull() {
        // Tình huống 1: Người dùng cũ từ thời xưa, trong Database cột Profile đang bị NULL
        mockUser.setProfile(null); 
        
        UserUpdateRequest req = new UserUpdateRequest();
        req.setFirstName("Vu"); // Chỉ gửi lên tên

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));
        when(userProfileRepository.save(any())).thenReturn(new UserProfile());
        
        userService.updateUserProfile(EMAIL, req);

        // Lúc này code sẽ chạy vào nhánh: if (profile == null) { profile = new UserProfile(); ... }
        assertNotNull(mockUser.getProfile(), "Hệ thống phải tự động tạo Profile mới nếu bị Null");
        assertEquals("Vu", mockUser.getProfile().getFirstName());
    }

    @Test
    void updateUserProfile_EmptyAndNullFields_ShouldNotUpdate() {
        // Tình huống 2: Người dùng đã có sẵn thông tin cũ
        UserProfile oldProfile = new UserProfile();
        oldProfile.setFirstName("OldName");
        oldProfile.setPhone("099999999");
        mockUser.setProfile(oldProfile);

        // Cố tình gửi một Request trống trơn, hoặc toàn phím cách (Space)
        UserUpdateRequest req = new UserUpdateRequest();
        req.setFirstName("");        // Rỗng
        req.setLastName("   ");      // Toàn dấu cách
        req.setPhone(null);          // Null
        req.setBio("");              // Rỗng

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));
        when(userProfileRepository.save(any())).thenReturn(oldProfile);

        userService.updateUserProfile(EMAIL, req);

        // Lúc này các nhánh if (...) sẽ bị FALSE hết. Code KHÔNG chạy vào trong.
        // Kết quả: Tên cũ và số điện thoại cũ PHẢI ĐƯỢC GIỮ NGUYÊN.
        assertEquals("OldName", mockUser.getProfile().getFirstName());
        assertEquals("099999999", mockUser.getProfile().getPhone());
    }
}
