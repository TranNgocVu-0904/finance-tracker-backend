package com.vutran.expensetracker.modules.user.dto;

import lombok.Data;

@Data
public class UserRegisterRequest {
    private String email;
    private String name;
    private String password;
}