package com.vutran.expensetracker.modules.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {
    
    // Mã Token lấy từ URL (ví dụ: ?token=abc-123)
    private String token;
    
    // Mật khẩu mới mà người dùng nhập vào
    private String newPassword;
}