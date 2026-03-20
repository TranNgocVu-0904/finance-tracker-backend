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
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder; 
import org.springframework.stereotype.Service;
import com.vutran.expensetracker.modules.category.entity.Category;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository; 
    private final PasswordEncoder passwordEncoder; 
    private final JwtUtils jwtUtils; 
    private final CategoryRepository categoryRepository;
    private final JavaMailSender mailSender;

    // Retrieve frontend URL from env, fallback to localhost if not found
    @Value("${FRONTEND_URL:http://127.0.0.1:5500}")
    private String frontendUrl;

    @Value("${spring.mail.password}")
    private String checkPass;

    @Value("${spring.datasource.url}")
    private String checkUrl;

    @PostConstruct
    public void verifyConfig() {
        System.out.println("======= CHECK CONFIGURATION =======");
        System.out.println("DB URL: " + checkUrl);
        System.out.println("Mail Pass: " + (checkPass != null ? "16-character code received" : "Empty"));
        System.out.println("FRONTEND URL: " + frontendUrl);
        System.out.println("=================================");
    }

    @Transactional
    public UserResponse registerUser(UserRegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("This email address is already in use!"); 
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User newUser = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(encodedPassword)
                .build();

        UserProfile profile = UserProfile.builder()
                .user(newUser)
                .build();
        newUser.setProfile(profile);

        User savedUser = userRepository.save(newUser);

        // Generate default categories for the new user
        List<Category> defaultCategories = List.of(
            Category.builder().name("Salary").type("INCOME").user(savedUser).build(),
            Category.builder().name("Bonus").type("INCOME").user(savedUser).build(),
            Category.builder().name("Food and Drink").type("EXPENSE").user(savedUser).build(),
            Category.builder().name("Transportation" ).type("EXPENSE").user(savedUser).build(),
            Category.builder().name("Housing").type("EXPENSE").user(savedUser).build(),
            Category.builder().name("Entertainment").type("EXPENSE").user(savedUser).build()
        );
        
        categoryRepository.saveAll(defaultCategories);

        return mapToResponse(savedUser);
    }

    @Transactional
    public UserResponse updateUserProfile(String email, UserUpdateRequest updateData) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found!"));

        UserProfile profile = user.getProfile();
        
        if (profile == null) {
            profile = new UserProfile();
            profile.setUser(user);
            user.setProfile(profile);
        }

        if (updateData.getFirstName() != null && !updateData.getFirstName().trim().isEmpty()) {
            profile.setFirstName(updateData.getFirstName());
        }
        if (updateData.getLastName() != null && !updateData.getLastName().trim().isEmpty()) {
            profile.setLastName(updateData.getLastName());
        }
        if (updateData.getPhone() != null && !updateData.getPhone().trim().isEmpty()) {
            profile.setPhone(updateData.getPhone());
        }
        if (updateData.getBio() != null && !updateData.getBio().trim().isEmpty()) {
            profile.setBio(updateData.getBio());
        }

        if (profile.getFirstName() != null && profile.getLastName() != null) {
            user.setName(profile.getFirstName() + " " + profile.getLastName());
        }

        userProfileRepository.save(profile); 
        userRepository.save(user); 

        return mapToResponse(user);
    }

    private UserResponse mapToResponse(User user) {
        UserProfile p = userProfileRepository.findById(user.getId()).orElse(null);

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .firstName(p != null ? p.getFirstName() : "")
                .lastName(p != null ? p.getLastName() : "")
                .phone(p != null ? p.getPhone() : "")
                .bio(p != null ? p.getBio() : "")
                .createdAt(user.getCreatedAt())
                .build();
    }

    public String login(UserRegisterRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Incorrect email or password!"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Incorrect email or password!");
        }

        return jwtUtils.generateTokenFromEmail(user.getEmail());
    }

    // Map to response to include full profile information
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found!"));
        
        return mapToResponse(user);
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found!"));

        // 1. Verify if the old password matches the one in the database
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new RuntimeException("The current password is incorrect!");
        }

        // 2. Ensure new password and confirmation password match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("The new passwords do not match!");
        }

        // 3. Encode the new password and save to database
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // --- FORGOT PASSWORD FLOW ---

    // 1. Process forgot password request
    @Transactional
    public void processForgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("The email address does not exist!"));

        // Generate a random UUID token and set expiration time (15 minutes from now)
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        // Send an email containing the reset link
        sendResetEmail(user.getEmail(), token);
    }

    // 2. Helper method to send the actual email
    private void sendResetEmail(String email, String token) {
        // Construct the dynamic link based on the environment (Local vs Production)
        String resetLink = frontendUrl + "/reset-password.html?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Password Recovery - Finance Tracker");
        message.setText("Hello,\n\nYou have requested a password recovery. Please click the link below to reset your password (The link is valid for 15 minutes):\n\n" + resetLink);
        
        mailSender.send(message);
    }

    // 3. Update password using the provided token
    @Transactional
    public void updatePasswordWithToken(String token, String newPassword) {
        // Find user by reset token
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("The verification token is invalid or has already been used!"));

        // Check if the token has expired
        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("The verification token has expired, please request a new one!");
        }

        // Encode the new password, clear the token to prevent reuse, and save
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        
        userRepository.save(user);
    }
}