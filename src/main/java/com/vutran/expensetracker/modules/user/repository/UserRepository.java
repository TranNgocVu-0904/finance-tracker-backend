package com.vutran.expensetracker.modules.user.repository;

import com.vutran.expensetracker.modules.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    // For Login (Retrieves User information to check password)
    Optional<User> findByEmail(String email);

    // For Registration (Checks if the email address is already taken)
    boolean existsByEmail(String email);

    Optional<User> findByResetToken(String token);
}