package com.vutran.expensetracker.modules.category.controller;

import com.vutran.expensetracker.modules.category.dto.CategoryRequest;
import com.vutran.expensetracker.modules.category.dto.CategoryResponse;
import com.vutran.expensetracker.modules.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
public ResponseEntity<List<CategoryResponse>> getAll() {
    // Trả về 200 OK kèm danh sách Category
    return ResponseEntity.ok(categoryService.getCategoriesByUser());
}

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@RequestBody CategoryRequest request) {
        // Hàm này sẽ gọi service để lưu Category gắn liền với User đang đăng nhập
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(request));
    }
}