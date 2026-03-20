package com.vutran.expensetracker.modules.analytics.service;

import com.vutran.expensetracker.modules.analytics.dto.AnalyticsResponse;
import com.vutran.expensetracker.modules.transaction.repository.TransactionRepository;
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
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AnalyticsServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private AnalyticsService analyticsService;

    private final String EMAIL = "test@vgu.edu.vn";

    @BeforeEach
    void setUp() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(EMAIL);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getDashboardData_Success() {
        when(transactionRepository.sumAmountByUserEmailAndCategoryType(EMAIL, "INCOME"))
                .thenReturn(new BigDecimal("5000"));
        when(transactionRepository.sumAmountByUserEmailAndCategoryType(EMAIL, "EXPENSE"))
                .thenReturn(new BigDecimal("2000"));

        AnalyticsResponse res = analyticsService.getDashboardData();
        assertEquals(new BigDecimal("5000"), res.getTotalIncome());
        assertEquals(new BigDecimal("2000"), res.getTotalExpense());
        assertEquals(new BigDecimal("3000"), res.getBalance());
    }

    @Test
    void getMonthlyDashboard_Success() {
        when(transactionRepository.sumAmountByDateRange(eq(EMAIL), eq("INCOME"), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("10000"));
        when(transactionRepository.sumAmountByDateRange(eq(EMAIL), eq("EXPENSE"), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("4000"));

        AnalyticsResponse res = analyticsService.getMonthlyDashboard(2026, 3);
        assertNotNull(res);
        assertEquals(new BigDecimal("6000"), res.getBalance());
    }
}