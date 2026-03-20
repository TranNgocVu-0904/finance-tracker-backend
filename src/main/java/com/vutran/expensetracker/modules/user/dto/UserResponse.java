package com.vutran.expensetracker.modules.user.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserResponse {
    private UUID id;
    private String email;
    private String name;
    
    private String firstName;
    private String lastName;
    private String phone;
    private String bio;
    
    private LocalDateTime createdAt;
}