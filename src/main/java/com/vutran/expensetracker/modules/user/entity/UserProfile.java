package com.vutran.expensetracker.modules.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    @Id
    private UUID id; // Sẽ dùng chung ID với User

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(length = 20)
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @OneToOne
    @MapsId // Đưa ID của User vào làm ID của Profile luôn
    @JoinColumn(name = "user_id")
    private User user;
}