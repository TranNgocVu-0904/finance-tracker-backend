package com.vutran.expensetracker.modules.category.service;

import com.vutran.expensetracker.modules.category.dto.CategoryRequest;
import com.vutran.expensetracker.modules.category.dto.CategoryResponse;
import com.vutran.expensetracker.modules.category.entity.Category;
import com.vutran.expensetracker.modules.category.repository.CategoryRepository;
import com.vutran.expensetracker.modules.user.entity.User;
import java.util.List;
import java.util.stream.Collectors;
import com.vutran.expensetracker.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CategoryResponse createCategory(CategoryRequest request) {
        String currentUserEmail = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found!"));

        Category category = Category.builder()
                .name(request.getName())
                .type(request.getType())
                .user(user)
                .build();

        Category saved = categoryRepository.save(category);
        
        return CategoryResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .type(saved.getType())
                .build();
    }


public List<CategoryResponse> getCategoriesByUser() {
    String email = org.springframework.security.core.context.SecurityContextHolder.getContext()
            .getAuthentication().getName();

    return categoryRepository.findByUserEmail(email).stream()
            .map(category -> CategoryResponse.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .type(category.getType())
                    .build())
            .collect(Collectors.toList());
}
}