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
    // Returns 200 OK along with a list of Categories
    return ResponseEntity.ok(categoryService.getCategoriesByUser());
}

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@RequestBody CategoryRequest request) {
        // This function will call a service to save the Category associated with the currently logged-in User.
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(request));
    }
}