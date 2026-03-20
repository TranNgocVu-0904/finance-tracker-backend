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

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository userRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private TransactionService transactionService;

    private User mockUser;
    private final String EMAIL = "test@vgu.edu.vn";

    @BeforeEach
    void setUp() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(EMAIL);
        SecurityContextHolder.setContext(securityContext);

        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setEmail(EMAIL);
    }

    // --- TEST TẠO GIAO DỊCH ---
    @Test
    void createTransaction_Success() {
        // 1. Lưu UUID vào một biến cố định để xài chung
        UUID fixedCategoryId = UUID.randomUUID();

        TransactionRequest req = new TransactionRequest();
        req.setCategoryId(fixedCategoryId); 
        req.setAmount(new BigDecimal("100"));
        req.setDescription("Test"); 
        
        // 2. SỬA LỖI Ở ĐÂY: Dùng LocalDate thay vì LocalDateTime
        req.setTransactionDate(java.time.LocalDate.now());

        Category cat = new Category(); cat.setUser(mockUser); cat.setName("Food");
        
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));
        
        // 3. Bắt Mockito tìm đúng cái fixedCategoryId đó
        when(categoryRepository.findById(fixedCategoryId)).thenReturn(Optional.of(cat));
        
        Transaction savedTx = new Transaction(); savedTx.setId(UUID.randomUUID()); savedTx.setCategory(cat);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTx);

        TransactionResponse res = transactionService.createTransaction(req);
        assertNotNull(res);
    }

    @Test
    void createTransaction_Fail_SecurityViolation() {
        // Tương tự, lưu UUID vào biến
        UUID fixedCategoryId = UUID.randomUUID();

        TransactionRequest req = new TransactionRequest(); 
        req.setCategoryId(fixedCategoryId);
        
        User otherUser = new User(); otherUser.setId(UUID.randomUUID());
        Category cat = new Category(); cat.setUser(otherUser); // Thuộc về người khác
        
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(mockUser));
        when(categoryRepository.findById(fixedCategoryId)).thenReturn(Optional.of(cat));

        assertThrows(RuntimeException.class, () -> transactionService.createTransaction(req));
    }

    // --- TEST XÓA GIAO DỊCH ---
    @Test
    void deleteTransaction_Success() {
        UUID txId = UUID.randomUUID();
        Transaction tx = new Transaction(); tx.setUser(mockUser);
        
        when(transactionRepository.findById(txId)).thenReturn(Optional.of(tx));
        transactionService.deleteTransaction(txId);
        
        verify(transactionRepository, times(1)).delete(tx);
    }

    @Test
    void deleteTransaction_Fail_SecurityViolation() {
        UUID txId = UUID.randomUUID();
        User otherUser = new User(); otherUser.setEmail("hacker@gmail.com");
        Transaction tx = new Transaction(); tx.setUser(otherUser); // Thuộc về người khác
        
        when(transactionRepository.findById(txId)).thenReturn(Optional.of(tx));

        assertThrows(RuntimeException.class, () -> transactionService.deleteTransaction(txId));
    }
    
    // --- TEST LẤY DANH SÁCH ---
    @Test
    void getAllTransactionsByUser_Success() {
        Category cat = new Category(); 
        cat.setName("Food and Drink");
        Transaction tx = new Transaction(); tx.setCategory(cat);
        
        when(transactionRepository.findByUserEmailOrderByTransactionDateDesc(EMAIL))
            .thenReturn(List.of(tx));
            
        List<TransactionResponse> list = transactionService.getAllTransactionsByUser();
        assertEquals(1, list.size());
    }
}