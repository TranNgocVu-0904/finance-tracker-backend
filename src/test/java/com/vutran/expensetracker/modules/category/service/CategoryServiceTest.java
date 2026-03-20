package com.vutran.expensetracker.modules.category.service;

import com.vutran.expensetracker.modules.category.dto.CategoryRequest;
import com.vutran.expensetracker.modules.category.dto.CategoryResponse;
import com.vutran.expensetracker.modules.category.entity.Category;
import com.vutran.expensetracker.modules.category.repository.CategoryRepository;
import com.vutran.expensetracker.modules.user.entity.User;
import com.vutran.expensetracker.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private final String MOCK_EMAIL = "sinhvien@vgu.edu.vn";
    private User mockUser;

    @BeforeEach
    void setUp() {
        // Giả lập người dùng đang đăng nhập
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(MOCK_EMAIL);
        SecurityContextHolder.setContext(securityContext);

        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setEmail(MOCK_EMAIL);
    }

    @Test
    void testCreateCategory_Success() {
        // Chuẩn bị dữ liệu gửi lên
        CategoryRequest request = new CategoryRequest();
        request.setName("Tiền đi chợ");
        request.setType("EXPENSE");

        // Dạy DB giả cách trả lời
        when(userRepository.findByEmail(MOCK_EMAIL)).thenReturn(Optional.of(mockUser));
        
        Category savedCategory = new Category();
        savedCategory.setId(UUID.randomUUID());
        savedCategory.setName("Tiền đi chợ");
        savedCategory.setType("EXPENSE");
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        // Gọi hàm thật
        CategoryResponse response = categoryService.createCategory(request);

        // Kiểm tra kết quả
        assertNotNull(response);
        assertEquals("Tiền đi chợ", response.getName());
        assertEquals("EXPENSE", response.getType());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void testGetCategoriesByUser_Success() {
        // Chuẩn bị danh sách Category giả
        Category cat1 = new Category();
        cat1.setId(UUID.randomUUID());
        cat1.setName("Salary"); cat1.setType("INCOME");
        
        Category cat2 = new Category();
        cat2.setId(UUID.randomUUID()); 
        cat2.setName("Food and Drink"); cat2.setType("EXPENSE");

        when(categoryRepository.findByUserEmail(MOCK_EMAIL)).thenReturn(List.of(cat1, cat2));

        // Gọi hàm thật
        List<CategoryResponse> responses = categoryService.getCategoriesByUser();

        // Kiểm tra kết quả
        assertEquals(2, responses.size());
        assertEquals("Salary", responses.get(0).getName());
    }
}