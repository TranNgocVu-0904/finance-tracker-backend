package com.vutran.expensetracker.modules.category.repository;

import com.vutran.expensetracker.modules.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    // Spring sẽ tự tạo câu SQL: SELECT * FROM categories WHERE user.email = ?
    List<Category> findByUserEmail(String email);
}