package com.vutran.expensetracker.modules.user.repository;

import com.vutran.expensetracker.modules.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    // Phục vụ cho Đăng nhập (Lấy thông tin User ra để kiểm tra mật khẩu)
    Optional<User> findByEmail(String email);

    // THÊM DÒNG NÀY: Phục vụ cho Đăng ký (Chỉ kiểm tra xem email đã bị trùng chưa)
    boolean existsByEmail(String email); 

    Optional<User> findByResetToken(String token);
}