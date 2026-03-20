package com.vutran.expensetracker.modules.user.controller;

import com.vutran.expensetracker.modules.user.dto.AuthResponse; 
import com.vutran.expensetracker.modules.user.dto.UserRegisterRequest;
import com.vutran.expensetracker.modules.user.dto.UserResponse;
import com.vutran.expensetracker.modules.user.dto.ResetPasswordRequest;
import com.vutran.expensetracker.modules.user.dto.UserUpdateRequest;
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

    // Inject the mail password from environment variables or system properties
    @Value("${SPRING_MAIL_PASSWORD:NOT_FOUND}") 
    private String mailPassword;

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegisterRequest request) {
        try {
            UserResponse response = userService.registerUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            
            // Handle exceptions and return a structured JSON error response
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

        // Extract the authenticated user's email from the security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = authentication.getName(); 
        
        UserResponse userInfo = userService.getUserByEmail(currentEmail);
        return ResponseEntity.ok(userInfo);
    }

    // Endpoint to update the current user's profile information
    @PutMapping("/update")
    public ResponseEntity<UserResponse> updateProfile(@RequestBody UserUpdateRequest request) {

        // Retrieve the email of the currently authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = authentication.getName();
        
        // Delegate the update operation to the user service
        UserResponse updatedUser = userService.updateUserProfile(currentEmail, request);
        
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentEmail = authentication.getName(); 
            
            userService.changePassword(currentEmail, request);
            
            // Return a success message upon password modification
            return ResponseEntity.ok(Map.of("message", "Password has been successfully changed."));
        } catch (Exception e) {
            // Return an error message if validation or authentication fails
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        
        // Debugging: Verify successful injection of the environment variable
        System.out.println("DEBUG: Injected mail password value is: " + mailPassword);
        
        try {
            String email = request.get("email");
            System.out.println("Processing password recovery request for: " + email); 
            
            userService.processForgotPassword(email);
            
            return ResponseEntity.ok(Map.of("message", "If your email address exists in our system, a recovery link has been sent."));
        } catch (Exception e) {
            e.printStackTrace(); 
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(Map.of("message", "Internal Server Error: " + e.getMessage()));
        }
    }

    // Endpoint to execute the password reset using the provided token and new credentials
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            userService.updatePasswordWithToken(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Your password has been successfully updated."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}