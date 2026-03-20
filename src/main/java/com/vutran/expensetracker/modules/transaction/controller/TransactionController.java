package com.vutran.expensetracker.modules.transaction.controller;

import com.vutran.expensetracker.modules.transaction.dto.TransactionRequest;
import com.vutran.expensetracker.modules.transaction.dto.TransactionResponse;
import com.vutran.expensetracker.modules.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    // Endpoint to create and persist a new transaction record
    @PostMapping
    public ResponseEntity<TransactionResponse> create(@Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.createTransaction(request));
    }

    // Retrieves all transactions associated with the currently authenticated user
    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getAll() {
        return ResponseEntity.ok(transactionService.getAllTransactionsByUser());
    }

    // Deletes a specific transaction by its unique identifier
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        transactionService.deleteTransaction(id);
        // Returns a 204 No Content status upon successful deletion
        return ResponseEntity.noContent().build(); 
    }
}