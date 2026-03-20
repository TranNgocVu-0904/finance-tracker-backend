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

    // API: Thêm mới một giao dịch
    @PostMapping
    public ResponseEntity<TransactionResponse> create(@Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.createTransaction(request));
    }

    // API: Lấy danh sách toàn bộ giao dịch của User đang đăng nhập
    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getAll() {
        return ResponseEntity.ok(transactionService.getAllTransactionsByUser());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        transactionService.deleteTransaction(id);
        // Trả về 204 No Content vì xóa xong thì không còn gì để hiển thị
        return ResponseEntity.noContent().build(); 
    }
}