package com.vutran.expensetracker.modules.transaction.service;

import com.vutran.expensetracker.modules.category.entity.Category;
import com.vutran.expensetracker.modules.category.repository.CategoryRepository;
import com.vutran.expensetracker.modules.transaction.dto.TransactionRequest;
import com.vutran.expensetracker.modules.transaction.dto.TransactionResponse;
import com.vutran.expensetracker.modules.transaction.entity.Transaction;
import com.vutran.expensetracker.modules.transaction.repository.TransactionRepository;
import com.vutran.expensetracker.modules.user.entity.User;
import com.vutran.expensetracker.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    // 1. CREATE A NEW TRANSACTION
    public TransactionResponse createTransaction(TransactionRequest request) {

        // Extract the authenticated user's email from the security context
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // Retrieve the user entity from the database
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Retrieve the category entity based on the provided identifier
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // SECURITY CHECK: Authorization validation to prevent IDOR vulnerabilities.
        // Deny access if the requested category belongs to a different user profile.
        if (!category.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Authorization Error: You do not have permission to utilize this category");
        }

        // Upon successful authorization, construct the Transaction entity
        Transaction transaction = Transaction.builder()
                .amount(request.getAmount())
                .description(request.getDescription())
                .transactionDate(request.getTransactionDate())
                .user(user)
                .category(category)
                .build();

        // Persist the transaction entity to the database
        Transaction savedTransaction = transactionRepository.save(transaction);

        // Map the persisted entity to a Data Transfer Object (DTO) for the client response
        return mapToResponse(savedTransaction);
    }

    // 2. RETRIEVE ALL TRANSACTIONS (ORDERED BY MOST RECENT)
    public List<TransactionResponse> getAllTransactionsByUser() {
        // Extract the authenticated user's email
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // Retrieve, map, and collect the transactions into a list
        return transactionRepository.findByUserEmailOrderByTransactionDateDesc(email)
                .stream()
                .map(this::mapToResponse) // Delegate to the helper method for entity-to-DTO mapping
                .collect(Collectors.toList());
    }
    
    // 3. DELETE A TRANSACTION
    public void deleteTransaction(UUID id) {
        // Extract the authenticated user's email
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // 1. Verify the existence of the transaction record
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found."));

        // 2. SECURITY CHECK: Authorize the deletion by verifying ownership of the transaction
        if (!transaction.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Authorization Error: You do not have permission to delete this transaction.");
        }

        // 3. Proceed with deletion upon successful authorization
        transactionRepository.delete(transaction);
    }

    // Helper Method: Map a Transaction entity to a TransactionResponse DTO
    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .transactionDate(transaction.getTransactionDate())
                .categoryName(transaction.getCategory().getName()) // Extract category name
                .categoryType(transaction.getCategory().getType()) // Extract category type (INCOME/EXPENSE)
                .build();
    }
}