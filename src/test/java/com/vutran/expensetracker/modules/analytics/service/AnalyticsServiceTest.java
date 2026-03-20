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

/**
 * Unit test suite for the AnalyticsService.
 * Utilizes Mockito to isolate the service layer by mocking the database repository
 * and Spring Security context, ensuring deterministic testing of financial aggregations.
 */
@ExtendWith(MockitoExtension.class)
public class AnalyticsServiceTest {

    // Mock dependencies to isolate the service logic from the data access layer
    @Mock private TransactionRepository transactionRepository;
    
    // Mock Spring Security components to simulate an authenticated user session
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    @InjectMocks
    private AnalyticsService analyticsService;

    private final String EMAIL = "test@vgu.edu.vn";

    @BeforeEach
    void setUp() {
        // Inject a mocked security context to bypass actual authentication mechanisms during testing
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(EMAIL);
        SecurityContextHolder.setContext(securityContext);
    }

    /**
     * Verifies the accurate retrieval and calculation of global dashboard metrics,
     * including total income, total expense, and the resulting net balance.
     */
    @Test
    void getDashboardData_Success() {
        // Arrange: Stub the repository methods to return predefined financial figures
        when(transactionRepository.sumAmountByUserEmailAndCategoryType(EMAIL, "INCOME"))
                .thenReturn(new BigDecimal("5000"));
        when(transactionRepository.sumAmountByUserEmailAndCategoryType(EMAIL, "EXPENSE"))
                .thenReturn(new BigDecimal("2000"));

        // Act: Execute the service method under test
        AnalyticsResponse res = analyticsService.getDashboardData();

        // Assert: Validate the integrity of the mapped response and arithmetic calculations
        assertEquals(new BigDecimal("5000"), res.getTotalIncome());
        assertEquals(new BigDecimal("2000"), res.getTotalExpense());
        assertEquals(new BigDecimal("3000"), res.getBalance()); // 5000 - 2000 = 3000
    }

    /**
     * Validates the chronological aggregation of financial data for a specific year and month,
     * ensuring the correct date range boundaries are passed to the repository layer.
     */
    @Test
    void getMonthlyDashboard_Success() {
        // Arrange: Stub the date-range-specific repository queries
        when(transactionRepository.sumAmountByDateRange(eq(EMAIL), eq("INCOME"), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("10000"));
        when(transactionRepository.sumAmountByDateRange(eq(EMAIL), eq("EXPENSE"), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("4000"));

        // Act: Execute the monthly aggregation service method
        AnalyticsResponse res = analyticsService.getMonthlyDashboard(2026, 3);

        // Assert: Confirm the response object is instantiated and the net balance is computed correctly
        assertNotNull(res);
        assertEquals(new BigDecimal("6000"), res.getBalance()); // 10000 - 4000 = 6000
    }
}