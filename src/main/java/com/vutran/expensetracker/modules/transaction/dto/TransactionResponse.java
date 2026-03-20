package com.vutran.expensetracker.modules.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private UUID id;
    private BigDecimal amount;
    private String description;
    private LocalDate transactionDate;
    private String categoryName; // Trả về tên Category cho FE dễ hiển thị (vd: "Ăn uống")
    private String categoryType; // Trả về loại (INCOME / EXPENSE)
}