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

    // RETRIEVE AGGREGATED DASHBOARD METRICS
    public AnalyticsResponse getDashboardData() {
        // 1. Extract the authenticated user's email from the security context
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Query the database to aggregate Total Income and Total Expense
        BigDecimal totalIncome = transactionRepository.sumAmountByUserEmailAndCategoryType(email, "INCOME");
        BigDecimal totalExpense = transactionRepository.sumAmountByUserEmailAndCategoryType(email, "EXPENSE");

        // 3. Calculate the Net Balance (Income - Expense)
        // NOTE: Utilizing the .subtract() method is mandatory to maintain precision in BigDecimal arithmetic operations
        BigDecimal balance = totalIncome.subtract(totalExpense);

        // 4. Encapsulate the aggregated data into a Data Transfer Object (DTO) for the client response
        return AnalyticsResponse.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .balance(balance)
                .build();
    }

    // RETRIEVE MONTHLY ANALYTICS DASHBOARD DATA
    public AnalyticsResponse getMonthlyDashboard(int year, int month) {
        // Extract the authenticated user's email
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // Calculate the chronological boundaries (start and end dates) for the specified month
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1); // First day of the specified month
        LocalDate endDate = yearMonth.atEndOfMonth(); // Last day of the month, inherently accounting for leap years and varying lengths

        // Execute database aggregations within the calculated date range
        BigDecimal totalIncome = transactionRepository.sumAmountByDateRange(email, "INCOME", startDate, endDate);
        BigDecimal totalExpense = transactionRepository.sumAmountByDateRange(email, "EXPENSE", startDate, endDate);
        
        // Compute the net balance
        BigDecimal balance = totalIncome.subtract(totalExpense);

        // Encapsulate and return the mapped DTO
        return AnalyticsResponse.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .balance(balance)
                .build();
    }
}