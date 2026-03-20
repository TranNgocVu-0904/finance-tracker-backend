package com.vutran.expensetracker.modules.analytics.service;

import com.vutran.expensetracker.modules.analytics.dto.AnalyticsResponse;
import com.vutran.expensetracker.modules.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final TransactionRepository transactionRepository;

    public AnalyticsResponse getDashboardData() {
        // 1. Lấy email của người dùng đang đăng nhập
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Nhờ Database tính Tổng Thu và Tổng Chi
        BigDecimal totalIncome = transactionRepository.sumAmountByUserEmailAndCategoryType(email, "INCOME");
        BigDecimal totalExpense = transactionRepository.sumAmountByUserEmailAndCategoryType(email, "EXPENSE");

        // 3. Tính Số dư (Balance = Income - Expense)
        // Lưu ý: Với BigDecimal, bạn phải dùng hàm .subtract() chứ không dùng dấu trừ (-) bình thường được
        BigDecimal balance = totalIncome.subtract(totalExpense);

        // 4. Đóng gói vào DTO và trả về
        return AnalyticsResponse.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .balance(balance)
                .build();
    }



    // THÊM HÀM NÀY VÀO TRONG ANALYTICS SERVICE
    public AnalyticsResponse getMonthlyDashboard(int year, int month) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // Tính ngày đầu tháng và ngày cuối tháng
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1); // Ngày 1 của tháng
        LocalDate endDate = yearMonth.atEndOfMonth(); // Ngày cuối cùng của tháng (28, 29, 30 hoặc 31)

        // Gọi DB tính toán trong khoảng thời gian này
        BigDecimal totalIncome = transactionRepository.sumAmountByDateRange(email, "INCOME", startDate, endDate);
        BigDecimal totalExpense = transactionRepository.sumAmountByDateRange(email, "EXPENSE", startDate, endDate);
        BigDecimal balance = totalIncome.subtract(totalExpense);

        return AnalyticsResponse.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .balance(balance)
                .build();
    }
}