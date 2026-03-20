package com.vutran.expensetracker.modules.analytics.controller;

import com.vutran.expensetracker.modules.analytics.dto.AnalyticsResponse;
import com.vutran.expensetracker.modules.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public ResponseEntity<AnalyticsResponse> getDashboard() {
        return ResponseEntity.ok(analyticsService.getDashboardData());
    }
    // THÊM API NÀY VÀO TRONG ANALYTICS CONTROLLER
    @GetMapping("/monthly")
    public ResponseEntity<AnalyticsResponse> getMonthlyDashboard(
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(analyticsService.getMonthlyDashboard(year, month));
    }
}