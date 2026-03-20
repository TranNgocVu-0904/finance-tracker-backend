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

    // Trong class UserService
    @Value("${spring.mail.password}")
    private String checkPass;

    @Value("${spring.datasource.url}")
    private String checkUrl;

    @PostConstruct
    public void verifyConfig() {
        System.out.println("======= KIỂM TRA CẤU HÌNH =======");
        System.out.println("DB URL: " + checkUrl);
        System.out.println("Mail Pass: " + (checkPass != null ? "Đã nhận mã 16 ký tự" : "Trống rỗng"));
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
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

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
                .orElseThrow(() -> new RuntimeException("Sai email hoặc mật khẩu!"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Sai email hoặc mật khẩu!");
        }

        return jwtUtils.generateTokenFromEmail(user.getEmail());
    }

    // ĐÃ SỬA: Gọi hàm mapToResponse để lấy đầy đủ thông tin Profile
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));
        
        return mapToResponse(user);
    }

    // Thêm hàm này vào cuối file UserService.java
    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));

        // 1. Kiểm tra mật khẩu cũ có khớp với Database không
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Mật khẩu hiện tại không đúng!");
        }

        // 2. Kiểm tra mật khẩu mới và xác nhận có khớp nhau không
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu xác nhận không khớp!");
        }

        // 3. Mã hóa mật khẩu mới và lưu xuống Database
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // 1. Xử lý yêu cầu quên mật khẩu
    @Transactional
    public void processForgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("The email address does not exist!"));

        // Tạo token ngẫu nhiên và đặt thời gian hết hạn (15 phút sau)
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        // Gửi email chứa link reset
        sendResetEmail(user.getEmail(), token);
    }

    // 2. Gửi mail thực tế (Helper method)
    private void sendResetEmail(String email, String token) {
        // Đường dẫn này sẽ dẫn về trang reset-password.html của bạn
        String resetLink = "http://127.0.0.1:5500/reset-password.html?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Khôi phục mật khẩu - Smart An Cư Advisor");
        message.setText("Xin chào,\n\nBạn đã yêu cầu khôi phục mật khẩu. Vui lòng click vào link bên dưới để đặt lại mật khẩu mới (Link có hiệu lực trong 15 phút):\n\n" + resetLink);
        
        mailSender.send(message);
    }

    // 3. Cập nhật mật khẩu bằng Token
    @Transactional
    public void updatePasswordWithToken(String token, String newPassword) {
        // Tìm user theo token (Bạn cần thêm hàm findByResetToken vào UserRepository)
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Mã xác nhận không hợp lệ hoặc đã được sử dụng!"));

        // Kiểm tra thời gian hết hạn
        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Mã xác nhận đã hết hạn, vui lòng yêu cầu lại!");
        }

        // Mã hóa mật khẩu mới và xóa token cũ để không dùng lại được nữa
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        
        userRepository.save(user);
    }
}