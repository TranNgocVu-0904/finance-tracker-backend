package com.vutran.expensetracker.modules.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {

    @NotNull(message = "Số tiền không được để trống")
    @Min(value = 1, message = "Số tiền phải lớn hơn 0")
    private BigDecimal amount;

    @NotBlank(message = "Ghi chú không được để trống")
    @Size(max = 255, message = "Ghi chú không được quá 255 ký tự")
    private String description;

    @NotNull(message = "Ngày giao dịch không được để trống")
    private LocalDate transactionDate;

    @NotNull(message = "Danh mục không được để trống")
    private UUID categoryId;
}