package com.vutran.expensetracker.modules.user.dto;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String firstName;
    private String lastName;
    private String phone;
    private String bio;
}