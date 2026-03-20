package com.vutran.expensetracker.modules.transaction.service;

import com.vutran.expensetracker.modules.category.entity.Category;
import com.vutran.expensetracker.modules.category.repository.CategoryRepository;
import com.vutran.expensetracker.modules.transaction.dto.TransactionRequest;
import com.vutran.expensetracker.modules.transaction.dto.TransactionResponse;
import com.vutran.expensetracker.modules.transaction.entity.Transaction;
import com.vutran.expensetracker.modules.transaction.repository.TransactionRepository;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test suite for the TransactionService.
 * This suite rigorously validates both the core financial business logic and the 
 * authorization boundaries, specifically preventing Insecure Direct Object Reference (IDOR) vulnerabilities.
 */
@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    // Mock data access dependencies
    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository userRepository;
    @Mock private CategoryRepository categoryRepository;
    
    // Mock security context components
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private TransactionService transactionService;

    private User mockUser;
    private final String EMAIL = "test@vgu.edu.vn";

    @BeforeEach
    void setUp() {
        // Initialize the mock security context to simulate an authenticated user session
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(EMAIL);
        SecurityContextHolder.setContext(securityContext);

        // Provision a standard mock user entity
        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setEmail(EMAIL);
    }

    // ==========================================
    // --- CREATE TRANSACTION TESTS ---
    // ==========================================

    /**
     * Verifies the successful creation of a transaction when all data inputs and authorization checks are valid.
     */
    @Test
    void createTransaction_Success() {
        // Arrange: Generate a fixed UUID to ensure consistency across the request and mock repository
        UUID fixedCategoryId = UUID.randomUUID();

        TransactionRequest req = new TransactionRequest();
        req.setCategoryId(fixedCategoryId); 
        req.setAmount(new BigDecimal("100"));
        req.setDescription("Test Payload"); 
        req.setTransactionDate(java.time.LocalDate.now());

        Category cat = new Category(); 
        cat.setUser(mockUser); 
        cat.setName("Food");
        
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));
        when(categoryRepository.findById(fixedCategoryId)).thenReturn(Optional.of(cat));
        
        Transaction savedTx = new Transaction(); 
        savedTx.setId(UUID.randomUUID()); 
        savedTx.setCategory(cat);
        
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTx);

        // Act: Execute the service method
        TransactionResponse res = transactionService.createTransaction(req);
        
        // Assert: Validate the response object
        assertNotNull(res);
    }

    /**
     * Simulates an IDOR attack during transaction creation.
     * Ensures the system correctly throws an exception when a user attempts to utilize a category they do not own.
     */
    @Test
    void createTransaction_Fail_SecurityViolation() {
        // Arrange: Construct an unauthorized request scenario
        UUID fixedCategoryId = UUID.randomUUID();

        TransactionRequest req = new TransactionRequest(); 
        req.setCategoryId(fixedCategoryId);
        
        // Simulate a category belonging to an entirely different user account
        User otherUser = new User(); 
        otherUser.setId(UUID.randomUUID());
        Category cat = new Category(); 
        cat.setUser(otherUser); 
        
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));
        when(categoryRepository.findById(fixedCategoryId)).thenReturn(Optional.of(cat));

        // Act & Assert: Verify that a RuntimeException is triggered to halt the unauthorized operation
        assertThrows(RuntimeException.class, () -> transactionService.createTransaction(req));
    }

    // ==========================================
    // --- DELETE TRANSACTION TESTS ---
    // ==========================================

    /**
     * Verifies that a user can successfully delete their own transaction record.
     */
    @Test
    void deleteTransaction_Success() {
        // Arrange: Prepare a mock transaction owned by the currently authenticated user
        UUID txId = UUID.randomUUID();
        Transaction tx = new Transaction(); 
        tx.setUser(mockUser);
        
        when(transactionRepository.findById(txId)).thenReturn(Optional.of(tx));
        
        // Act: Execute the deletion method
        transactionService.deleteTransaction(txId);
        
        // Assert: Confirm the repository's delete operation was invoked exactly once
        verify(transactionRepository, times(1)).delete(tx);
    }

    /**
     * Simulates an unauthorized deletion attempt.
     * Ensures the system blocks the action if the transaction target belongs to a different user.
     */
    @Test
    void deleteTransaction_Fail_SecurityViolation() {
        // Arrange: Prepare a target transaction owned by a presumed hacker/third-party
        UUID txId = UUID.randomUUID();
        User otherUser = new User(); 
        otherUser.setEmail("hacker@gmail.com");
        Transaction tx = new Transaction(); 
        tx.setUser(otherUser); 
        
        when(transactionRepository.findById(txId)).thenReturn(Optional.of(tx));

        // Act & Assert: Verify the security barrier throws an exception
        assertThrows(RuntimeException.class, () -> transactionService.deleteTransaction(txId));
    }
    
    // ==========================================
    // --- RETRIEVE TRANSACTIONS TESTS ---
    // ==========================================

    /**
     * Validates the retrieval and correct DTO mapping of all transactions associated with the user's account.
     */
    @Test
    void getAllTransactionsByUser_Success() {
        // Arrange: Stub a single mocked transaction payload
        Category cat = new Category(); 
        cat.setName("Food and Drink");
        Transaction tx = new Transaction(); 
        tx.setCategory(cat);
        
        when(transactionRepository.findByUserEmailOrderByTransactionDateDesc(EMAIL))
            .thenReturn(List.of(tx));
            
        // Act: Execute the retrieval method
        List<TransactionResponse> list = transactionService.getAllTransactionsByUser();
        
        // Assert: Confirm the collection size matches the stubbed repository data
        assertEquals(1, list.size());
    }
}