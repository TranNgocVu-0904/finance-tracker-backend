package com.vutran.expensetracker.modules.user.controller;

import com.vutran.expensetracker.modules.user.dto.AuthResponse; 

import com.vutran.expensetracker.modules.user.dto.UserRegisterRequest;
import com.vutran.expensetracker.modules.user.dto.UserResponse;
import com.vutran.expensetracker.modules.user.dto.ResetPasswordRequest;
import com.vutran.expensetracker.modules.user.dto.UserUpdateRequest; // Nhớ import DTO mới
import com.vutran.expensetracker.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import com.vutran.expensetracker.modules.user.dto.ChangePasswordRequest;
import java.util.Map;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    // 1. Spring sẽ tự tìm SPRING_MAIL_PASSWORD trong System Properties (mà Dotenv đã nạp)
    @Value("${SPRING_MAIL_PASSWORD:NOT_FOUND}") 
    private String mailPassword;

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegisterRequest request) {
        try {
            UserResponse response = userService.registerUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            // Bắt lỗi (ví dụ: "Email này đã được sử dụng!") và gửi về cho JavaScript
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserRegisterRequest request) {
        try {
            String token = userService.login(request);
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = authentication.getName(); 
        
        UserResponse userInfo = userService.getUserByEmail(currentEmail);
        return ResponseEntity.ok(userInfo);
    }

    // API MỚI: NHẬN REQUEST CẬP NHẬT TỪ FRONTEND
    @PutMapping("/update")
    public ResponseEntity<UserResponse> updateProfile(@RequestBody UserUpdateRequest request) {
        // Lấy email người dùng hiện tại từ Token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = authentication.getName();
        
        // Gọi Service xử lý
        UserResponse updatedUser = userService.updateUserProfile(currentEmail, request);
        
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentEmail = authentication.getName(); 
            
            userService.changePassword(currentEmail, request);
            
            // Trả về JSON thông báo thành công
            return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công!"));
        } catch (Exception e) {
            // Trả về JSON báo lỗi
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
  

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        
        // 2. Kiểm tra biến đã được "tiêm" vào chưa
        System.out.println("DEBUG: Mật khẩu lấy qua @Value là: " + mailPassword);
        
        try {
            String email = request.get("email");
            System.out.println("Processing forgot password for: " + email); 
            
            userService.processForgotPassword(email);
            
            return ResponseEntity.ok(Map.of("message", "If your email address exists, a recovery link has been sent."));
        } catch (Exception e) {
            e.printStackTrace(); 
            return ResponseEntity.status(500).body(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    // 2. API THỰC HIỆN ĐỔI MẬT KHẨU: Nhận Token và mật khẩu mới từ Frontend
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            userService.updatePasswordWithToken(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Your password has been successfully updated!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

}

