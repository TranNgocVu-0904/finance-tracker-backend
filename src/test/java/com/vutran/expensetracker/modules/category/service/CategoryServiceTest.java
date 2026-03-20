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

/**
 * Unit test suite for the CategoryService.
 * Validates the business logic for category management while isolating external dependencies 
 * using Mockito and verifying authentication flows.
 */
@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    // Mock data access layers to prevent actual database interactions
    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    // Inject mocked dependencies into the target service instance
    @InjectMocks
    private CategoryService categoryService;

    // Mock Spring Security components to simulate an active user session
    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private final String MOCK_EMAIL = "sinhvien@vgu.edu.vn";
    private User mockUser;

    @BeforeEach
    void setUp() {
        // Initialize the mocked authentication context to simulate a logged-in user
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(MOCK_EMAIL);
        SecurityContextHolder.setContext(securityContext);

        // Provision a standard mock user entity for subsequent test cases
        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setEmail(MOCK_EMAIL);
    }

    /**
     * Verifies the successful creation of a new category and ensures the entity 
     * is correctly mapped and persisted to the repository.
     */
    @Test
    void testCreateCategory_Success() {
        // Arrange: Prepare the incoming Data Transfer Object (DTO) payload
        CategoryRequest request = new CategoryRequest();
        request.setName("Tiền đi chợ");
        request.setType("EXPENSE");

        // Arrange: Stub the user repository to return the authenticated mock user
        when(userRepository.findByEmail(MOCK_EMAIL)).thenReturn(Optional.of(mockUser));
        
        // Arrange: Stub the category repository to simulate a successful save operation
        Category savedCategory = new Category();
        savedCategory.setId(UUID.randomUUID());
        savedCategory.setName("Tiền đi chợ");
        savedCategory.setType("EXPENSE");
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        // Act: Invoke the target method in the service layer
        CategoryResponse response = categoryService.createCategory(request);

        // Assert: Validate the response payload integrity
        assertNotNull(response);
        assertEquals("Tiền đi chợ", response.getName());
        assertEquals("EXPENSE", response.getType());
        
        // Assert: Verify that the repository's save method was executed exactly once
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    /**
     * Validates the retrieval of all categories associated with the currently authenticated user.
     */
    @Test
    void testGetCategoriesByUser_Success() {
        // Arrange: Construct a predefined list of mock category entities
        Category cat1 = new Category();
        cat1.setId(UUID.randomUUID());
        cat1.setName("Salary"); 
        cat1.setType("INCOME");
        
        Category cat2 = new Category();
        cat2.setId(UUID.randomUUID()); 
        cat2.setName("Food and Drink"); 
        cat2.setType("EXPENSE");

        // Arrange: Stub the repository query to return the mock list
        when(categoryRepository.findByUserEmail(MOCK_EMAIL)).thenReturn(List.of(cat1, cat2));

        // Act: Execute the retrieval method
        List<CategoryResponse> responses = categoryService.getCategoriesByUser();

        // Assert: Confirm the size and content of the returned collection
        assertEquals(2, responses.size());
        assertEquals("Salary", responses.get(0).getName());
    }
}